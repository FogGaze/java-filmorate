package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    @DisplayName("GET получение всех жанров")
    void getAllGenres() {
        Collection<Genre> genres = genreStorage.getAllGenres();
        assertEquals(6, genres.size());
    }

    @Test
    @DisplayName("GET жанры отсортированы по ID")
    void getAllGenresSortedById() {
        Collection<Genre> genres = genreStorage.getAllGenres();
        long prevId = 0;
        for (Genre genre : genres) {
            assertTrue(genre.getId() > prevId);
            prevId = genre.getId();
        }
    }

    @Test
    @DisplayName("GET получение жанра по ID")
    void getGenreById() {
        Genre genre = genreStorage.getGenreById(1);
        assertEquals(1L, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    @DisplayName("GET получение жанра по ID 6")
    void getGenreByIdSix() {
        Genre genre = genreStorage.getGenreById(6);
        assertEquals(6L, genre.getId());
        assertEquals("Боевик", genre.getName());
    }

    @Test
    @DisplayName("GET возвращает ошибку при некорректном ID жанра")
    void getGenreByIdNotFound() {
        assertThrows(NotFoundException.class, () -> genreStorage.getGenreById(999));
    }
}
