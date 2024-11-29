package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
class MpaDbStorageTests {
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void testGetAllMpa() {
        Collection<MpaRating> allMpa = mpaDbStorage.getAllMpa();

        assertThat(allMpa)
                .isNotEmpty()
                .allSatisfy(mpa -> assertThat(mpa).hasNoNullFieldsOrProperties());
    }

    @Test
    public void testGetCodeExists() {
        MpaRating mpaRating = mpaDbStorage.getCode(1L);

        assertThat(mpaRating)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrProperty("name")
                .satisfies(mpa -> assertThat(mpa.getName()).isNotBlank());
    }

    @Test
    public void testGetCodeNotFound() {
        Long nonExistentId = 999L;

        Throwable thrown = catchThrowable(() -> mpaDbStorage.getCode(nonExistentId));

        assertThat(thrown)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Рейтинг с таким Id не обнаружен!");
    }
}