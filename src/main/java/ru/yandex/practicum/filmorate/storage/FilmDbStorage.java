package ru.yandex.practicum.filmorate.storage;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Slf4j
@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private Connection getConnection() {
        try {
            return Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film addFilm(Film film) {
        Set<Long> genresID = new HashSet<>();
        Long rateId = null;

        if (film.getMpa() != null) {
            String getRateIdSql = "SELECT id FROM rates WHERE id = ?";
            try {
                Long id = film.getMpa().getId();
                rateId = jdbcTemplate.queryForObject(getRateIdSql, Long.class, id);
            } catch (EmptyResultDataAccessException e) {
                throw new ValidationException("Рейтинг " + film.getMpa().getId() + " не существует!");
            }
        } else {
            rateId = 1L;
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                String checkGenreSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
                Integer genreCount = jdbcTemplate.queryForObject(checkGenreSql, Integer.class, genre.getId());
                if (genreCount == 0) {
                    throw new ValidationException("Жанр с ID " + genre.getId() + " не существует!");
                }
                genresID.add(genre.getId());
            }
        }


        String insertFilmSql = "INSERT INTO films (name, description, release_date, duration, rate_id) " +
                "VALUES (?, ?, ?, ?, ?)";

//        jdbcTemplate.update(insertFilmSql, film.getName(), film.getDescription(),
//                film.getReleaseDate(), film.getDuration(), rateId);
//        String selectFilmId = "SELECT COUNT(*) FROM films";
//        Long filmId = jdbcTemplate.queryForObject(selectFilmId, Long.class);
        long filmId = 0L;
        try (
                PreparedStatement statement = getConnection().prepareStatement(insertFilmSql,
                        Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setLong(4, film.getDuration());
            statement.setLong(5, rateId);
            statement.executeUpdate();
            try (
                    ResultSet rs = statement.getGeneratedKeys();
            ) {
                while (rs.next()) {
                    filmId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Long genreId : genresID) {
            String insertFilmGenreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.update(insertFilmGenreSql, filmId, genreId);
        }

        film.setId(filmId);
        log.info("Создали фильм: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Ошибка при обновлении фильма: ID должен быть указан");
        }

        String checkFilmSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int count = jdbcTemplate.queryForObject(checkFilmSql, Integer.class, film.getId());
        if (count == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден!", film);
        }

        String updateFilmSql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, rate_id = ? WHERE id = ?";
        jdbcTemplate.update(updateFilmSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertGenresSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(insertGenresSql, film.getId(), genre.getId());
            }
        }
        log.info("Фильм с ID {} успешно обновлен: {}", film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(Film film) {
        String checkFilmSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkFilmSql, Integer.class, film.getId());
        if (count == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден!");
        }
        String deleteFilmSql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(deleteFilmSql, film.getId());
        log.info("Фильм с ID {} успешно удален.", film.getId());
    }

    @Override
    public Film getFilm(Long id) {
        if (id == null || id < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        String checkFilmSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int filmCount = jdbcTemplate.queryForObject(checkFilmSql, Integer.class, id);
        if (filmCount == 0) {
            Film notFoundFilm = new Film(id, "Unknown", "Unknown",
                    null, 0L, null);
            throw new NotFoundException("Пользователь с id " + id + " не найден.", notFoundFilm);
        }
        String sql = "SELECT * FROM films WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
    }

    @Override
    public void addLike(Long userId, Long filmId) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }

        String checkFilmSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int filmCount = jdbcTemplate.queryForObject(checkFilmSql, Integer.class, filmId);
        if (filmCount == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }

        String checkLikeSql = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND film_id = ?";
        int likeCount = jdbcTemplate.queryForObject(checkLikeSql, Integer.class, userId, filmId);
        if (likeCount > 0) {
            throw new AlreadyExistException("Пользователь уже поставил лайк этому фильму.");
        }

        String insertLikeSql = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(insertLikeSql, userId, filmId);
    }

    @Override
    public void removeLike(Long userId, Long filmId) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }

        String checkFilmSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int filmCount = jdbcTemplate.queryForObject(checkFilmSql, Integer.class, filmId);
        if (filmCount == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }

        String checkLikeSql = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND film_id = ?";
        int likeCount = jdbcTemplate.queryForObject(checkLikeSql, Integer.class, userId, filmId);
        if (likeCount == 0) {
            throw new NotFoundException("Лайк пользователя для данного фильма не найден.");
        }

        String deleteLikeSql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(deleteLikeSql, userId, filmId);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Long duration = rs.getLong("duration");
        String sql = "SELECT code FROM rates WHERE id = ?";
        Long rateId = rs.getLong("rate_id");
        MpaRating mpa = new MpaRating(rateId, jdbcTemplate.queryForObject(sql, String.class, rateId));
        Film film = new Film(id, name, description, releaseDate, duration, mpa);

        Set<Genre> genres = getGenresByFilmId(id);
        film.getGenres().addAll(genres);

        Set<Long> likes = getLikesByFilmId(id);
        film.getLikes().addAll(likes);

        return film;
    }

    public Set<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getLong("id"),
                rs.getString("name")), filmId));
    }

    public Set<Long> getLikesByFilmId(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("user_id"), filmId));
    }
}
