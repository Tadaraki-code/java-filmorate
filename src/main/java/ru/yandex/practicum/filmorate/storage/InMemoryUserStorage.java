package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    private final FilmStorage filmStorage;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAllUsers() {
        log.info("Вернули список всех пользователей");
        return users.values();
    }

    @Override
    public User addUser(User user) {
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

    @Override
    public User updateUser(User user) {
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

    @Override
    public void deleteUser(User user) {
        if (user.getId() == null || users.get(user.getId()) == null) {
            throw new NotFoundException(user);
        }
        for (User u : users.values()) {
            if (u.getFriends().contains(user.getId())) {
                u.getFriends().remove(user.getId());
            }
        }
        for (Film f : filmStorage.getAllFilms()) {
            if (f.getLikes().contains(user.getId())) {
                f.getLikes().remove(user.getId());
            }
        }
        users.remove(user.getId());
    }

    @Override
    public User getUser(Long id) {
        if (id == null || id < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException(new ErrorResponse("Фильм с ID " + id + " не найден!"));
        }
        return user;
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
