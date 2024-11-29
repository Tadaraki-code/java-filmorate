package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public User addUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistException("Аккаунт с таким email или логином уже существует", e);
        }

        sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, user.getEmail());
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Ошибка при обновлении пользователя: Id должен быть указан");
        }

        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, user.getId());
        if (userCount == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден.", user);
        }

        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        int emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, user.getEmail(), user.getId());

        if (emailCount > 0) {
            throw new AlreadyExistException("Ошибка при обновлении пользователя: этот email уже используется.");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Ошибка при обновлении пользователя: Логин не может содержать пробелы.");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName() != null ? user.getName() : user.getLogin(),  // Если имя не указано, ставим login
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Ошибка при обновлении пользователя: пользователь с id "
                    + user.getId() + " не найден.");
        }

        log.info("Обновлены данные пользователя с id {}", user.getId());
        return user;
    }

    @Override
    public void deleteUser(User user) {
        log.info("Удаляем пользователя с id {}", user.getId());
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, user.getId());

        if (rowsDeleted == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден.");
        }

        log.info("Пользователь с id {} был удалён", user.getId());
    }

    @Override
    public User getUser(Long id) {
        if (id == null || id < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, id);
        if (userCount == 0) {
            User notFoundUser = new User(id, "Unknown", "Unknown", "Unknown", null);
            throw new NotFoundException("Пользователь с id " + id + " не найден.", notFoundUser);
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
    }

    public Set<Long> getFriendsByUserId(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("friend_id"), userId));
    }

    public void addFriend(Long userId, Long friendId) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        int friendCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, friendId);


        if (userCount == 0) {
            User notFoundUser = new User(userId, "Unknown", "Unknown", "Unknown", null);
            throw new NotFoundException("Пользователь с id " + userId + " не найден.", notFoundUser);
        }
        if (friendCount == 0) {
            User notFoundUser = new User(friendId, "Unknown", "Unknown", "Unknown", null);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден.", notFoundUser);
        }

        User user = getUser(userId);
        User friend = getUser(friendId);

        String checkFriendSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        int friendCountInDb = jdbcTemplate.queryForObject(checkFriendSql, Integer.class, userId, friendId);

        if (friendCountInDb > 0) {
            throw new ValidationException("Этот пользователь уже добавлен в друзья.");
        }

        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, false);

        user.getFriends().add(friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        int friendCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, friendId);

        if (userCount == 0) {
            User notFoundUser = new User(userId, "Unknown", "Unknown", "Unknown", null);
            throw new NotFoundException("Пользователь с id " + userId + " не найден.", notFoundUser);
        }
        if (friendCount == 0) {
            User notFoundUser = new User(friendId, "Unknown", "Unknown", "Unknown", null);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден.", notFoundUser);
        }

        User user = getUser(userId);
        User friend = getUser(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, userId, friendId);

        user.getFriends().remove(friendId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();

        User user = new User(id, email, login, name, birthday);

        Set<Long> friends = getFriendsByUserId(id);
        user.getFriends().addAll(friends);
        return user;
    }
}
