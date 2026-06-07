package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {

        checkSameUserId(userId, friendId);

        User user = getUserById(userId);
        getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь с ID {} не может добавить в друзья пользователя {}, так как они уже друзья", userId, friendId);
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        user.getFriends().add(friendId);

        updateUser(user);
        log.trace("Пользователь с ID {} добавлен в список друзей пользователя {}", friendId, userId);
    }

    public void removeFriend(long userId, long friendId) {

        checkSameUserId(userId, friendId);

        User user = getUserById(userId);
        getUserById(friendId);

        user.getFriends().remove(friendId);

        updateUser(user);
        log.trace("Пользователь с ID {} удален из списка друзей пользователя {}", friendId, userId);
    }

    public List<User> getFriends(long userId) {

        User user = getUserById(userId);

        List<User> userFriends = userStorage.findUsers(user.getFriends());
        log.trace("Передан список друзей пользователя с ID {}", userId);
        return userFriends;
    }

    public List<User> getCommonFriends(long userId, long otherId) {

        User user = getUserById(userId);
        User other = getUserById(otherId);

        List<Long> commonFriends = user.getFriends().stream()
                .filter(friendId -> other.getFriends().contains(friendId))
                .collect(Collectors.toList());

        List<User> userFriends = userStorage.findUsers(commonFriends);

        log.trace("Передан список общих друзей пользователя с ID {} и пользователя {}", userId, otherId);
        return userFriends;
    }

    private void checkSameUserId(long userOneId, long userTwoId) {
        if (userOneId == userTwoId) {
            log.warn("Пользователь с ID {} не может взаимодействовать сам с собой", userOneId);
            throw new ValidationException("Пользователь не может взаимодействовать сам с собой");
        }
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }

    public void clearStorage() {
        userStorage.clearStorage();
    }
}
