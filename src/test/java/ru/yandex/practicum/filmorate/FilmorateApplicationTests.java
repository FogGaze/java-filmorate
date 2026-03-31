package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private long findID()
    Gson gson = new Gson();
    JsonObject json = gson.fromJson(responsePOST.getBody(), JsonObject.class);
    long id = json.get("id").getAsLong();

	@Test
    @DisplayName("GET возвращает пустую коллекцию фильмов")
	void getAllFilmsEmpty() {
        ResponseEntity<String> response = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("[]", response.getBody());
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(200, responsePOST.getStatusCodeValue());
        ResponseEntity<String> responsePOSTOne = restTemplate.postForEntity("/films", requestEntity(jsonInputOne), String.class);
        assertEquals(200, responsePOSTOne.getStatusCodeValue());

        ResponseEntity<String> responseGET = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, responseGET.getStatusCodeValue());
        assertTrue(responseGET.getBody().contains("\"name\":\"Фильм 1\""));
        assertTrue(responseGET.getBody().contains("\"name\":\"Фильм 2\""));
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(200, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("\"name\":\"Фильм 1\""));
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("Название не может быть пустым"));

        ResponseEntity<String> responseGET = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, responseGET.getStatusCodeValue());
        assertEquals("[]", responseGET.getBody());
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("Продолжительность должна быть положительной"));

        ResponseEntity<String> responseGET = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, responseGET.getStatusCodeValue());
        assertEquals("[]", responseGET.getBody());
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("Продолжительность должна быть положительной"));

        ResponseEntity<String> responseGET = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, responseGET.getStatusCodeValue());
        assertEquals("[]", responseGET.getBody());
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("Максимальная длина описания — 200 символов"));

        ResponseEntity<String> responseGET = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, responseGET.getStatusCodeValue());
        assertEquals("[]", responseGET.getBody());
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

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInput), String.class);
        assertEquals(400, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("Дата релиза — не раньше 28 декабря 1895 года"));

        ResponseEntity<String> responseGET = restTemplate.getForEntity("/films", String.class);
        assertEquals(200, responseGET.getStatusCodeValue());
        assertEquals("[]", responseGET.getBody());
    }

    @Test
    @DisplayName("PUT проходит успешно")
    void putDescriptionAndRelease() {
        String jsonInputPost = """
            {
                "name": "Фильм 1",
                "duration": 100
            }
            """;

        ResponseEntity<String> responsePOST = restTemplate.postForEntity("/films", requestEntity(jsonInputPost), String.class);
        assertEquals(200, responsePOST.getStatusCodeValue());
        assertTrue(responsePOST.getBody().contains("\"name\":\"Фильм 1\""));

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(responsePOST.getBody(), JsonObject.class);
        long id = json.get("id").getAsLong();

        String jsonInputPut = """
            {
                "id": %s,
                "description": "Очень хороший фильм",
                "releaseDate": "2010-01-01"
            }
            """.formatted(id).toString());

        ResponseEntity<String> responsePUT = restTemplate.exchange("/films", HttpMethod.PUT, requestEntity(jsonInputPut), String.class);
        assertEquals(200, responsePUT.getStatusCodeValue());
        assertTrue(responsePUT.getBody().contains("\"name\":\"Фильм 1\""));
        assertTrue(responsePUT.getBody().contains("\"duration\":\"100\""));
        assertTrue(responsePUT.getBody().contains("\"releaseDate\":\"2010-01-01\""));
        assertTrue(responsePUT.getBody().contains("\"description\":\"Очень хороший фильм\""));

    }

}
