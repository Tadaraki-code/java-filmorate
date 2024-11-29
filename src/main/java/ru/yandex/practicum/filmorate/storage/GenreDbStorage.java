package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Collection<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        log.info("Выполнили запрос на получение всех жанров");
        return jdbcTemplate.query(sql, this::mapRowToGenres);
    }

    public Genre getGenre(@PathVariable("id") Long id) {
        String checkRateSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        int count = jdbcTemplate.queryForObject(checkRateSql, Integer.class, id);
        if (count == 0) {
            log.info("В запрос на получение жанра по id,был передан не коректный id{}", id);
            throw new NotFoundException("Жанра с таким Id не обнаружен!", id);
        }
        String sql = "SELECT * FROM genres WHERE id = ?";
        log.info("Выполнили запрос на получение жанра по Id {}", id);
        return jdbcTemplate.queryForObject(sql, this::mapRowToGenres, id);
    }

    private Genre mapRowToGenres(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        return new Genre(id, name);
    }

}
