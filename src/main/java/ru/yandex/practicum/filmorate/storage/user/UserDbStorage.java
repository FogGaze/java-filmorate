package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExists;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public User addUser(User user) {
        checkEmailUniqueness(user.getEmail());
        if (user.getName() == null || user.getName().isBlank()) {
            log.trace("Имя пользователя не было передано. Установлено имя по умолчанию");
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() != null) {
                ps.setDate(4, Date.valueOf(user.getBirthday()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        log.trace("Пользователь {} с ID {} добавлен в базу данных", user.getLogin(), user.getId());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        User oldUser = getUserById(newUser.getId());

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

        String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE user_id=?";
        jdbcTemplate.update(sql,
                oldUser.getEmail(),
                oldUser.getLogin(),
                oldUser.getName(),
                oldUser.getBirthday() != null ? Date.valueOf(oldUser.getBirthday()) : null,
                oldUser.getId());

        syncFriends(oldUser);
        log.trace("Пользователь {}, ID {} обновлён в базе данных", oldUser.getLogin(), oldUser.getId());
        return oldUser;
    }

    @Override
    public void deleteUser(long id) {
        int deleted = jdbcTemplate.update("DELETE FROM users WHERE user_id=?", id);
        if (deleted == 0) {
            log.warn("Передано некорректное значение ID пользователя {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        log.trace("Пользователь с ID {} удалён из хранилища", id);
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        for (User user : users) {
            loadFriends(user);
        }
        log.trace("Передана коллекция всех пользователей");
        return users;
    }

    @Override
    public User getUserById(long id) {
        String sql = "SELECT * FROM users WHERE user_id=?";
        User user = jdbcTemplate.query(sql, userRowMapper, id).stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Передано некорректное значение ID пользователя {}", id);
                    return new NotFoundException("Пользователь с id = " + id + " не найден");
                });
        loadFriends(user);
        log.trace("Передан пользователь с ID {}", id);
        return user;
    }

    @Override
    public void clearStorage() {
        jdbcTemplate.update("DELETE FROM users");
        log.debug("Таблица users очищена");
    }

    @Override
    public List<User> findUsers(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM users WHERE user_id IN (" + inSql + ")";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, ids.toArray());
        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM user_friends WHERE user_id=?";
        List<Long> friendIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), user.getId());
        user.setFriends(new HashSet<>(friendIds));
    }

    private void syncFriends(User user) {
        jdbcTemplate.update("DELETE FROM user_friends WHERE user_id=?", user.getId());
        if (user.getFriends() != null) {
            String sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
            for (Long friendId : user.getFriends()) {
                jdbcTemplate.update(sql, user.getId(), friendId);
            }
        }
    }

    private void checkEmailUniqueness(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        if (count != null && count > 0) {
            log.warn("Пользователь с таким email {} уже существует", email);
            throw new AlreadyExists("Пользователь с таким email уже существует");
        }
    }
}
