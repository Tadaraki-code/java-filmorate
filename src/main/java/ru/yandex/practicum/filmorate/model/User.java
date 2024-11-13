package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class User implements DataModel {
    private Long id;
    @NotEmpty(message = "Поле Email не может быть пустым!")
    @Email(message = "Email имеет не надлежащий формат!")
    private String email;
    @NotEmpty(message = "Логин не может быть пустым!")
    private String login;
    private String name;
    @NotNull
    @Past(message = "Дата рождения не может быть в будущем!")
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();
}
