package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.OnCreate;
import ru.yandex.practicum.filmorate.model.OnUpdate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.trace("Получен запрос коллекции всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Positive long id) {
        log.trace("Получен запрос пользователя по id={}", id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable @Positive long id) {
        log.trace("Получен запрос списка друзей пользователя id={}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @Positive long id, @PathVariable @Positive long otherId) {
        log.trace("Получен запрос списка общих друзей пользователя id={} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User addUser(@Validated(OnCreate.class) @RequestBody User user) {
        log.trace("Получен запрос на добавление нового пользователя");
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Validated(OnUpdate.class) @RequestBody User newUser) {
        log.trace("Получен запрос на обновление существующего пользователя");
        return userService.updateUser(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive long id, @PathVariable @Positive long friendId) {
        log.trace("Получен запрос на добавления в друзья пользователей с id={}, {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive long id) {
        log.trace("Получен запрос на удаление пользователя с id={}", id);
        userService.deleteUser(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable @Positive long id, @PathVariable @Positive long friendId) {
        log.trace("Получен запрос на удаление пользователя с id={} из друзей пользователя {}", friendId, id);
        userService.removeFriend(id, friendId);
    }

    public void clearStorage() {
        userService.clearStorage();
    }
}
