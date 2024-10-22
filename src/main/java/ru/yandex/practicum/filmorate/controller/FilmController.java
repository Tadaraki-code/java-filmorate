package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class  FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Вернули список всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        for (Film f : films.values()) {
            if (f.getName().equals(film.getName()) & f.getReleaseDate().isEqual(film.getReleaseDate())) {
                throw new AlreadyExistException("Фильм с такой датой выхода и названием уже существует!");
            }
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создали фильм {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Ошибка при обновлении фильма:Id должен быть указан");
        }


        if (films.containsKey(film.getId())) {
            for (Film f : films.values()) {
                if (f.getName().equals(film.getName()) && f.getReleaseDate().isEqual(film.getReleaseDate())
                        && !(f.equals(film))) {
                    throw new AlreadyExistException("Ошибка при обновлении фильма:" +
                            "Фильм с такой датой выхода и названием уже существует!");
                }
            }
            Film oldFilm = films.get(film.getId());
            log.info("Обновляем данные о фильме{}", oldFilm);
            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());
            log.info("Обновленый фильм {}", oldFilm);
            return oldFilm;
        }
        throw new NotFoundException(film);

    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
