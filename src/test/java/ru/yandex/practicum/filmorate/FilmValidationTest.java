package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldNotViolateValidationConstraints() {
        Film film = new Film(1L, "Тест", "Тестовое описание",
                LocalDate.of(2010, 7, 16), 148L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldFailWhenNameIsEmpty() {
        Film film = new Film(1L, "", "A mind-bending thriller",
                LocalDate.of(2010, 7, 16), 148L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым!", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailWhenDescriptionIsTooLong() {
        String longDescription = "A".repeat(201); // 201 символ
        Film film = new Film(1L, "Тест", longDescription,
                LocalDate.of(2010, 7, 16), 148L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Длина описания для фильма не может превышать 200 символов!",
                violations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailWhenReleaseDateIsBeforeMinimumDate() {
        Film film = new Film(1L, "Тест", "Тестовое описание",
                LocalDate.of(1895, 12, 27), 120L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Дата выхода не может быть задана раньше этой даты 1895-12-28!",
                violations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailWhenDurationIsNegative() {
        Film film = new Film(1L, "Тест", "Тестовое описание",
                LocalDate.of(2010, 7, 16), -148L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма не может быть отрицательным числом!",
                violations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailWhenReleaseDateIsNull() {
        Film film = new Film(1L, "Тест", "Тестовое описание", null,
                148L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Поле с датой выхода не может быть пустым!", violations.iterator().next().getMessage());
    }
}
