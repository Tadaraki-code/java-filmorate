package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(film);
    }

    public Film getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    public void addLike(Long userId, Long filmId) {
        Film film = filmStorage.getFilm(filmId);
        userStorage.getUser(userId);

        if (!film.getLikes().contains(userId)) {
            film.getLikes().add(userId);
        } else {
            throw new AlreadyExistException("Пользователь уже поставил лайк этому фильму.");
        }
    }

    public void removeLike(Long userId, Long filmId) {
        Film film = filmStorage.getFilm(filmId);
        userStorage.getUser(userId);
        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
        } else {
            throw new NotFoundException("Лайк пользователя для данного фильма не найден.");
        }
    }

    public List<Film> getFilmsWithBiggestUsersLikes(int count) {
        if (count <= 0) {
            throw new ValidationException("Значение count должно быть больше нуля.");
        }

        return filmStorage.getAllFilms().stream()
                .sorted()
                .limit(count)
                .collect(Collectors.toList());
    }

}