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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
@Getter
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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
        TreeSet<Film> allFilms = new TreeSet<>(filmStorage.getAllFilms());
        List<Film> filmsRate = new ArrayList<>();
        if (count == 0 || count < 0) {
            throw new ValidationException("Невозможно сформировать рейтинг по лайкам, " +
                    "переданое значение являеться нулём.");
        }
        for (Film f : allFilms) {
            if (filmsRate.size() < count) {
                filmsRate.add(f);
            }
        }
        return filmsRate;
    }

}