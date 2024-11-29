package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;


@Slf4j
@Component
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Collection<MpaRating> getAllMpa() {
        String sql = "SELECT * FROM rates ORDER BY id;";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public MpaRating getCode(Long id) {
        String checkRateSql = "SELECT COUNT(*) FROM rates WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkRateSql, Integer.class, id);
        System.out.println(count);
        if (count == 0) {
            throw new NotFoundException("Рейтинг с таким Id не обнаружен!");
        }
        String sql = "SELECT * FROM rates WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id);
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("code");
        return new MpaRating(id, name);
    }
}
