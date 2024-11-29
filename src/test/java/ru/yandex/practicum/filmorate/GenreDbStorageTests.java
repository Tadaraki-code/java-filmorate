package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
class GenreDbStorageTests {
    private final GenreDbStorage genreDbStorage;

    @Test
    public void testGetAllGenres() {
        Collection<Genre> allGenres = genreDbStorage.getAllGenres();

        assertThat(allGenres)
                .isNotEmpty()
                .allSatisfy(genre ->
                        assertThat(genre)
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrProperty("id")
                                .hasFieldOrProperty("name")
                );
    }

    @Test
    public void testGetGenreExists() {
        Genre genre = genreDbStorage.getGenre(1L);

        assertThat(genre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrProperty("name")
                .satisfies(g -> assertThat(g.getName()).isNotBlank());
    }

    @Test
    public void testGetGenreNotFound() {
        Long nonExistentId = 999L;

        Throwable thrown = catchThrowable(() -> genreDbStorage.getGenre(nonExistentId));

        assertThat(thrown)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Жанра с таким Id не обнаружен!");
    }
}
