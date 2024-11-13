package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;


import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class  FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Вернули список всех фильмов");
        return filmService.getFilmStorage().getAllFilms();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.getFilmStorage().addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.getFilmStorage().updateFilm(film);
    }

    @DeleteMapping
    public void deleteFilm(@Valid @RequestBody Film film) {
        filmService.getFilmStorage().deleteFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") Long id) {
        return filmService.getFilmStorage().getFilm(id);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable("filmId") Long filmId, @PathVariable("userId") Long userId) {
        filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable("filmId") Long filmId, @PathVariable("userId") Long userId) {
        filmService.removeLike(userId, filmId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getFilmsWithBiggestUsersLikes(count);
    }
}
