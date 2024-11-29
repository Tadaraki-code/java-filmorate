package ru.yandex.practicum.filmorate;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;

    private final JdbcTemplate jdbcTemplate;

    Long userId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
    }

    @BeforeAll
    void beforeAll() {
        userId = createUserId();
    }

    @Test
    void testAddFilm() {
        Film film = createFilm(new Genre(1L, "Action"));

        Film createdFilm = filmDbStorage.addFilm(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Test Description");
    }

    @Test
    void testUpdateFilm() {
        Film film = createFilm(new Genre(1L, "Action"));
        Film createdFilm = filmDbStorage.addFilm(film);

        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");

        Film updatedFilm = filmDbStorage.updateFilm(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void testGetFilm() {
        Film film = createFilm(new Genre(1L, "Action"));
        Film createdFilm = filmDbStorage.addFilm(film);

        Film retrievedFilm = filmDbStorage.getFilm(createdFilm.getId());

        assertThat(retrievedFilm).isNotNull();
        assertThat(retrievedFilm.getId()).isEqualTo(createdFilm.getId());
        assertThat(retrievedFilm.getName()).isEqualTo(createdFilm.getName());
    }

    @Test
    void testDeleteFilm() {
        Film film = createFilm(new Genre(1L, "Action"));
        Film createdFilm = filmDbStorage.addFilm(film);

        filmDbStorage.deleteFilm(createdFilm);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> filmDbStorage.getFilm(createdFilm.getId()))
                .withMessage("Пользователь с id " + createdFilm.getId() + " не найден.");
    }

    @Test
    void testAddLike() {
        Film film = createFilm(new Genre(1L, "Action"));
        Film createdFilm = filmDbStorage.addFilm(film);

        filmDbStorage.addLike(userId, createdFilm.getId());

        Set<Long> likes = filmDbStorage.getFilm(createdFilm.getId()).getLikes();
        assertThat(likes).contains(userId);
    }

    @Test
    void testRemoveLike() {
        Film film = createFilm(new Genre(1L, "Action"));
        Film createdFilm = filmDbStorage.addFilm(film);

        filmDbStorage.addLike(userId, createdFilm.getId());
        filmDbStorage.removeLike(userId, createdFilm.getId());

        Set<Long> likes = filmDbStorage.getFilm(createdFilm.getId()).getLikes();
        assertThat(likes).doesNotContain(userId);
    }

    private Film createFilm(Genre genre) {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120L);
        film.setMpa(new MpaRating(1L, "G"));
        film.getGenres().add(genre);
        return film;
    }


    private long createUserId() {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        try (
                PreparedStatement statement = Objects.requireNonNull(jdbcTemplate.getDataSource())
                        .getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setString(1, "new@example.com");
            statement.setString(2, "newLogin");
            statement.setString(3, "New User");
            statement.setDate(4, Date.valueOf(LocalDate.of(2000, 1, 1)));

            statement.executeUpdate();

            try (
                    ResultSet rs = statement.getGeneratedKeys();
            ) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
