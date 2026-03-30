package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExists;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.OnCreate;
import ru.yandex.practicum.filmorate.model.OnUpdate;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.trace("Получен запрос коллекции всех пользователей");
        log.trace("Передана коллекция всех пользователей");
        return users.values();
    }

    @PostMapping
    public User addUser(@Validated(OnCreate.class) @RequestBody User user) {
        log.trace("Получен запрос на добавление нового пользователя");

        if (users.values().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail()))) {
            log.warn("Пользователь с таким email {} уже существует", user.getEmail());
            throw new AlreadyExists("Пользователь с таким email уже существует");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.trace("Имя пользователя не было передано. Установленно имя по умолчанию");
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        log.trace("Установлено ID {} для пользователя {}", user.getId(), user.getLogin());
        users.put(user.getId(), user);
        log.trace("Пользователь {} с ID {} добавлен в базу данных", user.getLogin(), user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Validated(OnUpdate.class) @RequestBody User newUser) {
        log.trace("Получен запрос на обновление существующего пользователя");

        if (!users.containsKey(newUser.getId())) {
            log.warn("Передано некорректное значение ID пользователя {}", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            log.trace("Имя пользователя {} изменено на {}", oldUser.getName(), newUser.getName());
            oldUser.setName(newUser.getName());
        }

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            if (users.values().stream()
                    .anyMatch(existingUser -> existingUser.getEmail().equals(newUser.getEmail())
                            && !existingUser.getId().equals(oldUser.getId()))) {
                log.warn("Пользователь с таким email {} уже существует", newUser.getEmail());
                throw new AlreadyExists("Пользователь с таким email уже существует");
            } else {
                log.trace("email пользователя {} изменен на {}", oldUser.getEmail(), newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            log.trace("Логин пользователя {} изменен на {}", oldUser.getLogin(), newUser.getLogin());
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            log.trace("Дата рождения пользователя обновлена");
            oldUser.setBirthday(newUser.getBirthday());
        }

        users.put(oldUser.getId(), oldUser);
        log.trace("Пользователь {}, ID {} обновлён в базе данных", oldUser.getLogin(), oldUser.getId());
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
