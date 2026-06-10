package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    @DisplayName("GET получение всех рейтингов")
    void getAllRatings() {
        Collection<Rating> ratings = mpaStorage.getAllRatings();
        assertEquals(5, ratings.size());
    }

    @Test
    @DisplayName("GET рейтинги отсортированы по ID")
    void getAllRatingsSortedById() {
        Collection<Rating> ratings = mpaStorage.getAllRatings();
        long prevId = 0;
        for (Rating rating : ratings) {
            assertTrue(rating.getId() > prevId);
            prevId = rating.getId();
        }
    }

    @Test
    @DisplayName("GET получение рейтинга по ID")
    void getRatingById() {
        Rating rating = mpaStorage.getRatingById(1);
        assertEquals(1L, rating.getId());
        assertEquals("G", rating.getName());
    }

    @Test
    @DisplayName("GET получение рейтинга по ID 5")
    void getRatingByIdFive() {
        Rating rating = mpaStorage.getRatingById(5);
        assertEquals(5L, rating.getId());
        assertEquals("NC-17", rating.getName());
    }

    @Test
    @DisplayName("GET возвращает ошибку при некорректном ID рейтинга")
    void getRatingByIdNotFound() {
        assertThrows(NotFoundException.class, () -> mpaStorage.getRatingById(999));
    }
}
