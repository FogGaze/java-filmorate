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
        ResponseEntity<User> response = restTemplate.postForEntity("/user", requestEntity(json), User.class);
        assertEquals(200, response.getStatusCodeValue());
        return response.getBody();
    }

    private User updateUser(String json) {
        ResponseEntity<User> putResponse = restTemplate.exchange("/user", HttpMethod.PUT, requestEntity(json), User.class);
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

}
