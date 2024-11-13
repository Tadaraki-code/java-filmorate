package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        log.info("Вернули список всех фильмов");
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
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

    @Override
    public Film updateFilm(Film film) {
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

    @Override
    public void deleteFilm(Film film) {
        if (film.getId() == null || films.get(film.getId()) == null) {
            throw new NotFoundException(film);
        }
        films.remove(film.getId());
    }

    public Film getFilm(Long id) {
        if (id == null || id < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        if (films.get(id) == null) {
            throw new NotFoundException(new ErrorResponse("Пользователь с ID " + id + " не найден!"));
        }
        return films.get(id);
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
