package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;

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

    //GET /films
	@Test
    @DisplayName("возвращает коллекцию всех")
	void getAllFilms() {
	}

}
