package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.LocalDateValidator;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film implements DataModel {
    private Long id;
    @NotEmpty(message = "Название фильма не может быть пустым!")
    private String name;
    @NotNull
    @Size(max = 200, message = "Длина описания для фильма не может превышать 200 символов!")
    private String description;
    @NotNull(message = "Поле с датой выхода не может быть пустым!")
    @LocalDateValidator(value = "1895-12-28", message = "Дата выхода не может быть задана раньше этой даты 1895-12-28!")
    private LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность фильма не может быть отрицательным числом!")
    private Long duration;
}
