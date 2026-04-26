package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExists;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {

        checkEmailUniqueness(user.getEmail());

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

    @Override
    public User updateUser(User newUser) {

        checkIdUser(newUser.getId());

        User oldUser = users.get(newUser.getId());

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            log.trace("Имя пользователя {} изменено на {}", oldUser.getName(), newUser.getName());
            oldUser.setName(newUser.getName());
        }

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            if (!newUser.getEmail().equals(oldUser.getEmail())) {
                checkEmailUniqueness(newUser.getEmail());
            }
            log.trace("email пользователя {} изменен на {}", oldUser.getEmail(), newUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            log.trace("Логин пользователя {} изменен на {}", oldUser.getLogin(), newUser.getLogin());
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            log.trace("Дата рождения пользователя обновлена");
            oldUser.setBirthday(newUser.getBirthday());
        }

        if (newUser.getFriends() != null) {
            log.trace("Список друзей пользователя c ID {} обновлен", oldUser.getId());
            oldUser.setFriends(newUser.getFriends());
        }

        users.put(oldUser.getId(), oldUser);
        log.trace("Пользователь {}, ID {} обновлён в базе данных", oldUser.getLogin(), oldUser.getId());
        return oldUser;
    }

    @Override
    public void deleteUser(long id) {
        checkIdUser(id);

        User removed = users.get(id);
        users.remove(id);
        log.trace("Пользователь {} с ID {} удалён из хранилища", removed.getName(), removed.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        log.trace("Передана коллекция всех пользователей");
        return users.values();
    }

    @Override
    public User getUserById(long id) {
        checkIdUser(id);
        log.trace("Передан пользователь с ID {}", id);
        return users.get(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkIdUser(long id) {
        if (!users.containsKey(id)) {
            log.warn("Передано некорректное значение ID пользователя {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    private void checkEmailUniqueness(String email) {
        if (users.values().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equals(email))) {
            log.warn("Пользователь с таким email {} уже существует", email);
            throw new AlreadyExists("Пользователь с таким email уже существует");
        }
    }

    @Override
    public void clearStorage() {
        users.clear();
        log.debug("Хранилище хэш мап очищено");
    }

}
