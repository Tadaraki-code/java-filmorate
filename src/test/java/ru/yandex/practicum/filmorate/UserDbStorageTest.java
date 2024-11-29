package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private final JdbcTemplate jdbcTemplate;

    Long userId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testGetAllUsers() {
        createUserId("test@example.com", "test_login");

        Collection<User> users = userStorage.getAllUsers();
        assertThat(users).hasSize(1);
    }

    @Test
    void testAddUser() {
        User user = new User(null, "test@example.com", "testLogin", "Test User",
                LocalDate.of(1990, 1, 1));
        User addedUser = userStorage.addUser(user);

        assertThat(addedUser).isNotNull();
        assertThat(addedUser.getId()).isNotNull();
        assertThat(addedUser.getLogin()).isEqualTo("testLogin");
    }

    @Test
    void testAddUserWithValidationError() {
        User user = new User(null, "test@example.com", "invalid login", "Test User",
                LocalDate.of(1990, 1, 1));
        assertThrows(ValidationException.class, () -> userStorage.addUser(user));
    }

    @Test
    void testUpdateUser() {
        long userId = createUserId("test2@example.com", "test_login2");

        User updatedUser = new User(userId, "updated@example.com", "updatedLogin", "Updated Name", LocalDate.of(1991, 2, 2));
        User result = userStorage.updateUser(updatedUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testUpdateUserNotFound() {
        User nonExistentUser = new User(999L, "nonexistent@example.com", "nonexistent", "Nonexistent User", LocalDate.of(1990, 1, 1));
        assertThrows(NotFoundException.class, () -> userStorage.updateUser(nonExistentUser));
    }

    @Test
    void testDeleteUser() {
        long userId = createUserId("test3@example.com", "test_login3");

        User user = userStorage.getUser(userId);
        userStorage.deleteUser(user);

        assertThrows(NotFoundException.class, () -> userStorage.getUser(userId));
    }

    @Test
    void testGetUser() {
        long userId = createUserId("test4@example.com", "test_login4");

        User user = userStorage.getUser(userId);
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
    }

    @Test
    void testGetFriendsByUserId() {
        long userId = createUserId("test5@example.com", "test_login5");
        long friendId = createUserId("test6@example.com", "test_login6");

        userStorage.addFriend(userId, friendId);
        Set<Long> friends = userStorage.getFriendsByUserId(userId);

        assertThat(friends).contains(friendId);
    }

    @Test
    void testAddFriend() {
        long userId = createUserId("test7@example.com", "test_login7");
        long friendId = createUserId("test8@example.com", "test_login8");

        userStorage.addFriend(userId, friendId);

        Set<Long> friends = userStorage.getFriendsByUserId(userId);
        assertThat(friends).contains(friendId);
    }

    @Test
    void testDeleteFriend() {
        long userId = createUserId("test9@example.com", "test_login9");
        long friendId = createUserId("test10@example.com", "test_login10");

        userStorage.addFriend(userId, friendId);
        userStorage.deleteFriend(userId, friendId);

        Set<Long> friends = userStorage.getFriendsByUserId(userId);
        assertThat(friends).doesNotContain(friendId);
    }

    private long createUserId(String email, String login) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        try (
                PreparedStatement statement = Objects.requireNonNull(jdbcTemplate.getDataSource())
                        .getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setString(1, email);
            statement.setString(2, login);
            statement.setString(3, "New User");
            statement.setDate(4, Date.valueOf(LocalDate.of(2000, 1, 1)));

            statement.executeUpdate();

            try (
                    ResultSet rs = statement.getGeneratedKeys();
            ) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
