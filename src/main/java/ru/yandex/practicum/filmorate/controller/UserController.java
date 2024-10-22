package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        log.info("Вернули список всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        for (User u : users.values()) {
            if (u.getEmail().equals(user.getEmail())) {
                throw new AlreadyExistException("Аккаунт с таким Email уже существует");
            }
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        log.info("Создали пользователя {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("Ошибка при обновлении пользователя:Id должен быть указан");
        }

        if (users.containsKey(user.getId())) {
            for (User u : users.values()) {
                if (u.getEmail().equals(user.getEmail()) && !(u.equals(user))) {
                    throw new AlreadyExistException("Ошибка при обновлении пользователя :этот имейл уже используется");
                }
            }

            User oldUser = users.get(user.getId());
            log.info("Обновляем данные пользователя{}", oldUser);
            if (!user.getLogin().contains(" ")) {
                oldUser.setLogin(user.getLogin());
            } else {
                throw new ValidationException("Ошибка при обновлении пользователя:Логин содержит пробелы: " +
                        user.getLogin());
            }
            if (user.getName() != null) {
                oldUser.setName(user.getName());
            } else {
                oldUser.setName(user.getLogin());
            }
            oldUser.setEmail(user.getEmail());
            oldUser.setBirthday(user.getBirthday());
            log.info("Обновили данные пользователя{}", oldUser);
            return oldUser;
        }
        throw new NotFoundException(user);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
