package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAfterValidator implements ConstraintValidator<LocalDateValidator, LocalDate> {

    private LocalDate baseDate;

    @Override
    public void initialize(LocalDateValidator constraintAnnotation) {
        this.baseDate = LocalDate.parse(constraintAnnotation.value(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.isAfter(baseDate);
    }
}
