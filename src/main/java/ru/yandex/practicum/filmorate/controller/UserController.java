package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getAll() {
        log.info("Вернули список всех пользователей");
        return userService.getUserStorage().getAllUsers();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.getUserStorage().addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.getUserStorage().updateUser(user);
    }

    @DeleteMapping
    public void delete(@Valid @RequestBody User user) {
        userService.getUserStorage().deleteUser(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userService.getUserStorage().getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        userService.addFriend(id,friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        userService.deleteFriend(id,friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<User> getAllFriends(@PathVariable("id") Long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getAllCommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        return userService.getAllCommonFriends(id,otherId);
    }

}
//PUT /users/{id}/friends/{friendId} — добавление в друзья.
//DELETE /users/{id}/friends/{friendId} — удаление из друзей.
//GET /users/{id}/friends — возвращаем список пользователей, являющихся его друзьями.
//GET /users/{id}/friends/common/{otherId} — список друзей, общих с другим пользователем.