package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
public class FilmService {

    @Autowired
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;

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
        filmStorage.addLike(userId, filmId);
    }

    public void removeLike(Long userId, Long filmId) {
        filmStorage.removeLike(userId, filmId);
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