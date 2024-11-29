package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Getter
public class UserService {

    @Autowired
    @Qualifier("userDbStorage")
    private UserStorage userStorage;

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void deleteUser(User user) {
        userStorage.deleteUser(user);
    }

    public User getUser(Long id) {
        return userStorage.getUser(id);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userStorage.deleteFriend(userId, friendId);
    }

    public Set<User> getAllCommonFriends(Long userId, Long anotherUserId) {
        User user1 = userStorage.getUser(userId);
        User user2 = userStorage.getUser(anotherUserId);

        Set<Long> userOneFriends = user1.getFriends();
        Set<Long> userTwoFriends = user2.getFriends();

        Set<Long> commonFriendIds = new HashSet<>(userOneFriends);
        commonFriendIds.retainAll(userTwoFriends);

        return commonFriendIds.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toSet());
    }

    public Set<User> getAllFriends(Long userId) {
        User user1 = userStorage.getUser(userId);
        Set<Long> userFriends = user1.getFriends();
        return userFriends.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toSet());
    }

}
