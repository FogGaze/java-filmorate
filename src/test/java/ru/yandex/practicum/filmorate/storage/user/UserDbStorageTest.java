package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.AlreadyExists;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage.clearStorage();
    }

    private User makeUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    @DisplayName("POST создание пользователя")
    void addUser() {
        User user = makeUser("practicum@yandex.ru", "Petr", "Пётр", LocalDate.of(1995, 1, 1));
        User created = userStorage.addUser(user);

        assertNotNull(created.getId());
        assertEquals("practicum@yandex.ru", created.getEmail());
        assertEquals("Petr", created.getLogin());
        assertEquals("Пётр", created.getName());
        assertEquals(LocalDate.of(1995, 1, 1), created.getBirthday());
    }

    @Test
    @DisplayName("POST создание пользователя email и логин")
    void addUserEmailAndLogin() {
        User user = makeUser("practicum@yandex.ru", "Petr", null, null);
        User created = userStorage.addUser(user);

        assertEquals("Petr", created.getLogin());
        assertEquals("Petr", created.getName());
        assertEquals("practicum@yandex.ru", created.getEmail());
        assertNull(created.getBirthday());
    }

    @Test
    @DisplayName("POST не создаёт пользователя при дублирование email")
    void addUserDuplicateEmail() {
        User user = makeUser("practicum@yandex.ru", "Petr", null, null);
        userStorage.addUser(user);

        User duplicate = makeUser("practicum@yandex.ru", "Ivan", null, null);
        assertThrows(AlreadyExists.class, () -> userStorage.addUser(duplicate));
    }

    @Test
    @DisplayName("GET получение пользователя по ID")
    void getUserById() {
        User user = makeUser("practicum@yandex.ru", "Petr", "Пётр", LocalDate.of(1995, 1, 1));
        User created = userStorage.addUser(user);

        User found = userStorage.getUserById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("practicum@yandex.ru", found.getEmail());
        assertEquals("Petr", found.getLogin());
        assertEquals("Пётр", found.getName());
        assertEquals(LocalDate.of(1995, 1, 1), found.getBirthday());
    }

    @Test
    @DisplayName("GET возвращает ошибку при некорректном ID")
    void getUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getUserById(999));
    }

    @Test
    @DisplayName("PUT обновление данных пользователя, имя и дата рождения")
    void updateUserPartial() {
        User user = makeUser("practicum@yandex.ru", "Petr", null, null);
        User created = userStorage.addUser(user);
        assertEquals("Petr", created.getName());

        User update = new User();
        update.setId(created.getId());
        update.setName("Пётр");
        update.setBirthday(LocalDate.of(1995, 1, 1));

        User result = userStorage.updateUser(update);
        assertEquals("Пётр", result.getName());
        assertEquals(LocalDate.of(1995, 1, 1), result.getBirthday());
        assertEquals("Petr", result.getLogin());
        assertEquals("practicum@yandex.ru", result.getEmail());
    }

    @Test
    @DisplayName("PUT обновление всех данных пользователя")
    void updateUserFull() {
        User user = makeUser("practicum@yandex.ru", "Petr", "Пётр", LocalDate.of(1995, 1, 1));
        User created = userStorage.addUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setEmail("practicum1990@yandex.ru");
        update.setLogin("Ivan");
        update.setName("Иван");
        update.setBirthday(LocalDate.of(1990, 1, 1));

        User result = userStorage.updateUser(update);
        assertEquals("practicum1990@yandex.ru", result.getEmail());
        assertEquals("Ivan", result.getLogin());
        assertEquals("Иван", result.getName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthday());
    }

    @Test
    @DisplayName("PUT возвращает ошибку при некорректном ID")
    void updateUserNotFound() {
        User update = new User();
        update.setId(999L);
        update.setName("Пётр");

        assertThrows(NotFoundException.class, () -> userStorage.updateUser(update));
    }

    @Test
    @DisplayName("PUT возвращает ошибку при дублирование email")
    void updateUserDuplicateEmail() {
        userStorage.addUser(makeUser("practicum@yandex.ru", "Petr", "Пётр", null));
        User second = userStorage.addUser(makeUser("practicum1990@yandex.ru", "Ivan", "Иван", null));

        User update = new User();
        update.setId(second.getId());
        update.setEmail("practicum@yandex.ru");

        assertThrows(AlreadyExists.class, () -> userStorage.updateUser(update));
    }

    @Test
    @DisplayName("DELETE удаление пользователя")
    void deleteUser() {
        User user = makeUser("practicum@yandex.ru", "Petr", null, null);
        User created = userStorage.addUser(user);

        userStorage.deleteUser(created.getId());
        assertThrows(NotFoundException.class, () -> userStorage.getUserById(created.getId()));
    }

    @Test
    @DisplayName("DELETE возвращает ошибку при некорректном ID")
    void deleteUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.deleteUser(999));
    }

    @Test
    @DisplayName("GET получение пустой коллекции пользователей")
    void getAllUsersEmpty() {
        Collection<User> users = userStorage.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("GET получение всех пользователей")
    void getAllUsers() {
        userStorage.addUser(makeUser("practicum@yandex.ru", "Petr", "Пётр", null));
        userStorage.addUser(makeUser("practicum2@yandex.ru", "Ivan", "Иван", null));
        userStorage.addUser(makeUser("practicum3@yandex.ru", "Pavel", "Павел", null));

        Collection<User> users = userStorage.getAllUsers();
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("DELETE очистка хранилища")
    void clearStorage() {
        userStorage.addUser(makeUser("practicum@yandex.ru", "Petr", null, null));
        userStorage.clearStorage();

        Collection<User> users = userStorage.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Друзья пользователя")
    void friendsAreLoaded() {
        User user1 = userStorage.addUser(makeUser("practicum@yandex.ru", "Petr", null, null));
        User user2 = userStorage.addUser(makeUser("practicum1@yandex.ru", "Ivan", null, null));

        user1.getFriends().add(user2.getId());
        userStorage.updateUser(user1);
        user2.getFriends().add(user1.getId());
        userStorage.updateUser(user2);

        User found = userStorage.getUserById(user1.getId());
        assertThat(found.getFriends()).containsExactly(user2.getId());
    }

    @Test
    @DisplayName("GET поиск нескольких пользователей по ID")
    void findUsers() {
        User user1 = userStorage.addUser(makeUser("practicum@yandex.ru", "Petr", null, null));
        User user2 = userStorage.addUser(makeUser("practicum1@yandex.ru", "Ivan", null, null));
        userStorage.addUser(makeUser("practicum3@yandex.ru", "Pavel", null, null));

        List<User> found = userStorage.findUsers(List.of(user1.getId(), user2.getId()));
        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("GET поиск пользователей с пустым списком ID")
    void findUsersEmpty() {
        List<User> found = userStorage.findUsers(List.of());
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("GET поиск пользователей с несуществующими ID")
    void findUsersNotFound() {
        userStorage.addUser(makeUser("practicum@yandex.ru", "Petr", null, null));
        List<User> found = userStorage.findUsers(List.of(999L));
        assertTrue(found.isEmpty());
    }
}
