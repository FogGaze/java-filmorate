package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    @BeforeEach
    void preparingStorage() {
        filmController.clearStorage();
        userController.clearStorage();
    }

    private HttpEntity<String> requestEntity(String jsonInput) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonInput, headers);
        return requestEntity;
    }

    private Film createFilm(String json) {
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", requestEntity(json), Film.class);
        assertEquals(200, response.getStatusCodeValue());
        return response.getBody();
    }

    private Film updateFilm(String json) {
        ResponseEntity<Film> putResponse = restTemplate.exchange("/films", HttpMethod.PUT, requestEntity(json), Film.class);
        assertEquals(200, putResponse.getStatusCodeValue());
        return putResponse.getBody();
    }

    private User createUser(String json) {
        ResponseEntity<User> response = restTemplate.postForEntity("/users", requestEntity(json), User.class);
        assertEquals(200, response.getStatusCodeValue());
        return response.getBody();
    }

    private User updateUser(String json) {
        ResponseEntity<User> putResponse = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(json), User.class);
        assertEquals(200, putResponse.getStatusCodeValue());
        return putResponse.getBody();
    }

    //films
    @Test
    @DisplayName("GET возвращает пустую коллекцию фильмов")
    void getAllFilmsEmpty() {
        ResponseEntity<Film[]> getResponse = restTemplate.getForEntity("/films", Film[].class);
        Film[] films = getResponse.getBody();
        assertEquals(0, getResponse.getBody().length);
    }

    @Test
    @DisplayName("GET возвращает коллекцию всех фильмов")
    void getAllFilms() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        String jsonInputOne = """
                {
                    "name": "Фильм 2",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        Film filmOne = createFilm(jsonInput);
        assertEquals("Фильм 1", filmOne.getName());

        Film filmTwo = createFilm(jsonInputOne);
        assertEquals("Фильм 2", filmTwo.getName());

        ResponseEntity<Film[]> getResponse = restTemplate.getForEntity("/films", Film[].class);
        Film[] films = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCodeValue());
        assertNotNull(films);
        assertEquals(2, films.length);
    }

    @Test
    @DisplayName("POST только имя и продолжительность")
    void postNameAndDurationFilm() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "duration": 100
                }
                """;

        Film filmOne = createFilm(jsonInput);
        assertEquals("Фильм 1", filmOne.getName());
    }

    @Test
    @DisplayName("POST Фильм без имени не добавляется")
    void postFilmNoName() {
        String jsonInput = """
                {
                    "name": "",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Название не может быть пустым"));

        ResponseEntity<String> getResponse = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals("[]", getResponse.getBody());
    }

    @Test
    @DisplayName("POST Фильм без продолжительности не добавляется")
    void postFilmNoDuration() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01"
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Продолжительность должна быть положительной"));

        ResponseEntity<String> getResponse = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals("[]", getResponse.getBody());
    }

    @Test
    @DisplayName("POST Фильм с отрицательной продолжительностью не добавляется")
    void postFilmNegativeDuration() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": -1
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Продолжительность должна быть положительной"));

        ResponseEntity<String> getResponse = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals("[]", getResponse.getBody());
    }

    @Test
    @DisplayName("POST Фильм с описанием >200 символов не добавляется")
    void postFilmBigDescription() {
        String description = "Очень хороший фильм".repeat(200);
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "%s",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """.formatted(description);

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Максимальная длина описания — 200 символов"));

        ResponseEntity<String> getResponse = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals("[]", getResponse.getBody());
    }

    @Test
    @DisplayName("POST Фильм с релизом ранее 28 декабря 1895 года не добавляется")
    void postFilmEarlyReleaseDate() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "1895-01-01",
                    "duration": 100
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Дата релиза — не раньше 28 декабря 1895 года"));

        ResponseEntity<String> getResponse = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals("[]", getResponse.getBody());
    }

    @Test
    @DisplayName("PUT обновляет значения")
    void putDescriptionAndRelease() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);

        String jsonInputPut = """
                {
                    "id": %s,
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01"
                }
                """.formatted(film.getId());

        Film filmUpdate = updateFilm(jsonInputPut);
        assertEquals("Фильм 1", filmUpdate.getName());
        assertEquals(100, filmUpdate.getDuration());
        assertEquals("Очень хороший фильм", filmUpdate.getDescription());
        assertEquals(LocalDate.of(2010, 01, 01), filmUpdate.getReleaseDate());
    }

    @Test
    @DisplayName("PUT обновляет только одно значение")
    void putDescription() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);

        String jsonInputPut = """
                {
                    "id": %s,
                    "description": "Отличное кино"
                }
                """.formatted(film.getId());

        Film filmUpdate = updateFilm(jsonInputPut);
        assertEquals("Фильм 1", filmUpdate.getName());
        assertEquals(100, filmUpdate.getDuration());
        assertEquals("Отличное кино", filmUpdate.getDescription());
        assertEquals(LocalDate.of(2010, 01, 01), filmUpdate.getReleaseDate());
    }

    @Test
    @DisplayName("PUT обновляет все значения")
    void putUpdateAll() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);

        String jsonInputPut = """
                {
                    "id": %s,
                    "name": "Фильм Один",
                    "description": "Отличное кино",
                    "releaseDate": "2020-02-02",
                    "duration": 200
                }
                """.formatted(film.getId());

        Film filmUpdate = updateFilm(jsonInputPut);
        assertEquals("Фильм Один", filmUpdate.getName());
        assertEquals(200, filmUpdate.getDuration());
        assertEquals("Отличное кино", filmUpdate.getDescription());
        assertEquals(LocalDate.of(2020, 02, 02), filmUpdate.getReleaseDate());
    }

    @Test
    @DisplayName("PUT не обновляет несуществующий фильм")
    void putNoRealFilm() {
        String jsonInputPut = """
                {
                    "id": 1,
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01"
                }
                """;

        ResponseEntity<String> putResponse = restTemplate.exchange("/films", HttpMethod.PUT, requestEntity(jsonInputPut), String.class);
        assertEquals(404, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Фильм с id = 1 не найден"));
    }

    @Test
    @DisplayName("PUT не обновляет без ID")
    void putNoIdFilm() {
        String jsonInputPut = """
                {
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01"
                }
                """;

        ResponseEntity<String> putResponse = restTemplate.exchange("/films", HttpMethod.PUT, requestEntity(jsonInputPut), String.class);
        assertEquals(400, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("ID обязателен при обновлении"));
    }

    @Test
    @DisplayName("PUT не обновляет значения с неверной датой")
    void putWrongRelease() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);

        String jsonInputPut = """
                {
                    "id": %s,
                    "description": "Очень хороший фильм",
                    "releaseDate": "1000-01-01"
                }
                """.formatted(film.getId());

        ResponseEntity<String> putResponse = restTemplate.exchange("/films", HttpMethod.PUT, requestEntity(jsonInputPut), String.class);
        assertEquals(400, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    @DisplayName("PUT не обновляет значения с некорректным описанием")
    void putWrongDescription() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);
        assertEquals("Очень хороший фильм", film.getDescription());

        String description = "Очень хороший фильм".repeat(200);
        String jsonInputPut = """
                {
                    "id": %s,
                    "description": "%s"
                }
                """.formatted(film.getId(), description);

        ResponseEntity<String> putResponse = restTemplate.exchange("/films", HttpMethod.PUT, requestEntity(jsonInputPut), String.class);
        assertEquals(400, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Максимальная длина описания — 200 символов"));
    }

    @Test
    @DisplayName("PUT не обновит не положительную продолжительность")
    void putWrongDuration() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);

        String jsonInputPut = """
                {
                    "id": %s,
                    "duration": 0
                }
                """.formatted(film.getId());

        Film filmUpdate = updateFilm(jsonInputPut);
        assertEquals("Фильм 1", filmUpdate.getName());
        assertEquals(100, filmUpdate.getDuration());
        assertEquals("Очень хороший фильм", filmUpdate.getDescription());
        assertEquals(LocalDate.of(2010, 01, 01), filmUpdate.getReleaseDate());
    }

    @Test
    @DisplayName("PUT не обновит пустое имя")
    void putWrongName() {
        String jsonInput = """
                {
                    "name": "Фильм 1",
                    "description": "Очень хороший фильм",
                    "releaseDate": "2010-01-01",
                    "duration": 100
                }
                """;

        Film film = createFilm(jsonInput);

        String jsonInputPut = """
                {
                    "id": %s,
                    "name": ""
                }
                """.formatted(film.getId());

        Film filmUpdate = updateFilm(jsonInputPut);
        assertEquals("Фильм 1", filmUpdate.getName());
        assertEquals(100, filmUpdate.getDuration());
        assertEquals("Очень хороший фильм", filmUpdate.getDescription());
        assertEquals(LocalDate.of(2010, 01, 01), filmUpdate.getReleaseDate());
    }

    //user
    @Test
    @DisplayName("POST создание пользователя")
    void postAddUser() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;

        User user = createUser(jsonInput);
        assertEquals("Petr", user.getLogin());
        assertEquals("Пётр", user.getName());
        assertEquals("practicum@yandex.ru", user.getEmail());
        assertEquals(LocalDate.of(1995, 01, 01), user.getBirthday());
    }

    @Test
    @DisplayName("POST создание пользователя, email и логин")
    void postAddUserEmailAndLogin() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr"
                }
                """;

        User user = createUser(jsonInput);
        assertEquals("Petr", user.getLogin());
        assertEquals("Petr", user.getName());
        assertEquals("practicum@yandex.ru", user.getEmail());
        assertNull(user.getBirthday());
    }

    @Test
    @DisplayName("POST не создаёт пользователя при дублирование email")
    void postAddDoubleUser() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr"
                }
                """;

        createUser(jsonInput);

        String jsonInputTwo = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Ivan"
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/users", requestEntity(jsonInputTwo), String.class);
        assertEquals(409, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Пользователь с таким email уже существует"));
    }

    @Test
    @DisplayName("POST возвращает ошибку при некорректном логине")
    void postAddUserWrongLogin() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "P e t r ",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/users", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Логин не должен содержать пробелов"));
    }

    @Test
    @DisplayName("POST возвращает ошибку при некорректном email")
    void postAddUserWrongEmail() {
        String jsonInput = """
                {
                    "email": "practicumyandexru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/users", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Некорректный email"));
    }

    @Test
    @DisplayName("POST возвращает ошибку при дате рождения в будущем")
    void postAddUserWrongBirthday() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "2995-01-01"
                }
                """;

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/users", requestEntity(jsonInput), String.class);
        assertEquals(400, postResponse.getStatusCodeValue());
        assertTrue(postResponse.getBody().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("GET получение пустой коллекции пользователей")
    void getEmptyUsers() {

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity("/users", User[].class);
        User[] users = getResponse.getBody();
        assertEquals(0, users.length);
    }

    @Test
    @DisplayName("GET получение всех пользователей")
    void getAllUsers() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr"
                }
                """;
        createUser(jsonInput);

        String jsonInputTwo = """
                {
                    "email": "practicum2@yandex.ru",
                    "login": "Ivan"
                }
                """;
        createUser(jsonInputTwo);

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity("/users", User[].class);
        User[] users = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCodeValue());
        assertNotNull(users);
        assertEquals(2, users.length);
    }

    @Test
    @DisplayName("PUT обновление данных пользователя, имя и дата рождения")
    void putUpdateUserNameAndBirthDay() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr"
                }
                """;

        User user = createUser(jsonInput);
        assertEquals("Petr", user.getLogin());
        assertEquals("Petr", user.getName());
        assertEquals("practicum@yandex.ru", user.getEmail());
        assertNull(user.getBirthday());

        String jsonInputUpdate = """
                {
                    "id": %s,
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """.formatted(user.getId());

        User userUpdate = updateUser(jsonInputUpdate);
        assertEquals("Petr", userUpdate.getLogin());
        assertEquals("Пётр", userUpdate.getName());
        assertEquals("practicum@yandex.ru", userUpdate.getEmail());
        assertEquals(LocalDate.of(1995, 01, 01), userUpdate.getBirthday());

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity("/users", User[].class);
        User[] users = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCodeValue());
        assertNotNull(users);
        assertEquals(1, users.length);
    }

    @Test
    @DisplayName("PUT обновление всех данных пользователя")
    void putUpdateAllUser() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;

        User user = createUser(jsonInput);
        assertEquals("Petr", user.getLogin());
        assertEquals("Пётр", user.getName());
        assertEquals("practicum@yandex.ru", user.getEmail());
        assertEquals(LocalDate.of(1995, 01, 01), user.getBirthday());

        String jsonInputUpdate = """
                {
                    "id": %s,
                    "email": "practicum1990@yandex.ru",
                    "login": "Ivan",
                    "name": "Иван",
                    "birthday": "1990-01-01"
                }
                """.formatted(user.getId());

        User userUpdate = updateUser(jsonInputUpdate);
        assertEquals("Ivan", userUpdate.getLogin());
        assertEquals("Иван", userUpdate.getName());
        assertEquals("practicum1990@yandex.ru", userUpdate.getEmail());
        assertEquals(LocalDate.of(1990, 01, 01), userUpdate.getBirthday());

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity("/users", User[].class);
        User[] users = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCodeValue());
        assertNotNull(users);
        assertEquals(1, users.length);
    }

    @Test
    @DisplayName("PUT возвращает исключения. id некорректный")
    void putUpdateUserWrongId() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr"
                }
                """;
        createUser(jsonInput);

        String jsonInputUpdate = """
                {
                    "id": 56,
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;

        ResponseEntity<String> putResponse = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(jsonInputUpdate), String.class);
        assertEquals(404, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Пользователь с id = 56 не найден"));

        String jsonInputUpdateTwo = """
                {
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;

        ResponseEntity<String> putResponseTwo = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(jsonInputUpdateTwo), String.class);
        assertEquals(400, putResponseTwo.getStatusCodeValue());
        assertTrue(putResponseTwo.getBody().contains("ID обязателен при обновлении"));

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity("/users", User[].class);
        User[] users = getResponse.getBody();
        assertEquals(200, getResponse.getStatusCodeValue());
        assertNotNull(users);
        assertEquals(1, users.length);
    }

    @Test
    @DisplayName("PUT возвращает ошибку при дублирование email")
    void putUpdateUserEmailDouble() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;
        User user = createUser(jsonInput);
        assertEquals("practicum@yandex.ru", user.getEmail());

        String jsonInputTwo = """
                {
                    "email": "practicum1990@yandex.ru",
                    "login": "Ivan",
                    "name": "Иван",
                    "birthday": "1995-01-01"
                }
                """;
        User userTwo = createUser(jsonInputTwo);
        assertEquals("practicum1990@yandex.ru", userTwo.getEmail());

        String jsonInputUpdate = """
                {
                    "id": %s,
                    "email": "practicum@yandex.ru"
                }
                """.formatted(userTwo.getId());

        ResponseEntity<String> putResponse = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(jsonInputUpdate), String.class);
        assertEquals(409, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Пользователь с таким email уже существует"));
    }

    @Test
    @DisplayName("PUT возвращает ошибку при некорректном email")
    void putAddUserWrongEmail() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;
        User user = createUser(jsonInput);
        assertEquals("practicum@yandex.ru", user.getEmail());

        String jsonInputUpdate = """
                {
                    "id": %s,
                    "email": "practicumyandexru"
                }
                """.formatted(user.getId());

        ResponseEntity<String> putResponse = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(jsonInputUpdate), String.class);
        assertEquals(400, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Некорректный email"));
    }

    @Test
    @DisplayName("PUT возвращает ошибку при дате рождения в будущем")
    void putAddUserWrongBirthday() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;
        User user = createUser(jsonInput);
        assertEquals(LocalDate.of(1995, 01, 01), user.getBirthday());

        String jsonInputUpdate = """
                {
                    "id": %s,
                    "birthday": "2995-01-01"
                }
                """.formatted(user.getId());

        ResponseEntity<String> putResponse = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(jsonInputUpdate), String.class);
        assertEquals(400, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("PUT возвращает ошибку при некорректном логине")
    void putAddUserWrongLogin() {
        String jsonInput = """
                {
                    "email": "practicum@yandex.ru",
                    "login": "Petr",
                    "name": "Пётр",
                    "birthday": "1995-01-01"
                }
                """;
        User user = createUser(jsonInput);
        assertEquals("Petr", user.getLogin());

        String jsonInputUpdate = """
                {
                    "id": %s,
                    "login": "P e t r"
                }
                """.formatted(user.getId());

        ResponseEntity<String> putResponse = restTemplate.exchange("/users", HttpMethod.PUT, requestEntity(jsonInputUpdate), String.class);
        assertEquals(400, putResponse.getStatusCodeValue());
        assertTrue(putResponse.getBody().contains("Логин не должен содержать пробелов"));
    }

}
