package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidUser() {
        User user = new User(1L, "cool@example.com", "Tadaraki",
                "Sergey", LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Ожидаем, что нарушений не будет
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidEmail() {
        User user = new User(1L, "invalid-email", "Tadaraki",
                "Sergey", LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Ожидаем нарушение валидации email
        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Email имеет не надлежащий формат!", violation.getMessage());
    }

    @Test
    public void testEmptyEmail() {
        User user = new User(1L, "", "Tadaraki", "Sergey",
                LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Ожидаем нарушение валидации для пустого email
        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Поле Email не может быть пустым!", violation.getMessage());
    }

    @Test
    public void testEmptyLogin() {
        User user = new User(1L, "cool@example.com", "", "Sergey",
                LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Ожидаем нарушение валидации для пустого логина
        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Логин не может быть пустым!", violation.getMessage());
    }

    @Test
    public void testFutureBirthday() {
        User user = new User(1L, "cool@example.com", "Tadaraki", "Sergey",
                LocalDate.of(2090, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Ожидаем нарушение валидации для даты рождения в будущем
        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Дата рождения не может быть в будущем!", violation.getMessage());
    }
}
