package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage.clearStorage();
        userStorage.clearStorage();
    }

    private Film makeFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }

    @Test
    @DisplayName("POST создание фильма")
    void addFilm() {
        Film film = makeFilm("Фильм 1", "описание", LocalDate.of(2000, 1, 1), 120);
        Film created = filmStorage.addFilm(film);

        assertNotNull(created.getId());
        assertEquals("Фильм 1", created.getName());
        assertEquals("описание", created.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), created.getReleaseDate());
        assertEquals(120, created.getDuration());
    }

    @Test
    @DisplayName("POST создание фильма без описания")
    void addFilmWithoutDescription() {
        Film film = makeFilm("Фильм 1", null, null, 90);
        Film created = filmStorage.addFilm(film);

        assertEquals("Фильм 1", created.getName());
        assertNull(created.getDescription());
        assertNull(created.getReleaseDate());
        assertEquals(90, created.getDuration());
    }

    @Test
    @DisplayName("POST Фильм с релизом ранее 28 декабря 1895 года не добавляется")
    void addFilmEarlyRelease() {
        Film film = makeFilm("Фильм 1", "описание", LocalDate.of(1800, 1, 1), 100);
        assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
    }

    @Test
    @DisplayName("GET получение фильма по ID")
    void getFilmById() {
        Film film = makeFilm("Фильм 1", "описание", LocalDate.of(2010, 6, 15), 150);
        Film created = filmStorage.addFilm(film);

        Film found = filmStorage.getFilmById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Фильм 1", found.getName());
        assertEquals("описание", found.getDescription());
        assertEquals(LocalDate.of(2010, 6, 15), found.getReleaseDate());
        assertEquals(150, found.getDuration());
    }

    @Test
    @DisplayName("GET возвращает ошибку при некорректном ID")
    void getFilmByIdNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getFilmById(999));
    }

    @Test
    @DisplayName("PUT обновляет описание и дату")
    void updateFilmPartial() {
        Film film = makeFilm("Фильм 1", "старое описание", LocalDate.of(2000, 1, 1), 100);
        Film created = filmStorage.addFilm(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setDescription("новое описание");
        update.setReleaseDate(LocalDate.of(2005, 5, 5));

        Film result = filmStorage.updateFilm(update);
        assertEquals("Фильм 1", result.getName());
        assertEquals("новое описание", result.getDescription());
        assertEquals(LocalDate.of(2005, 5, 5), result.getReleaseDate());
        assertEquals(100, result.getDuration());
    }

    @Test
    @DisplayName("PUT не обновит не положительную продолжительность")
    void updateFilmIgnoreZeroDuration() {
        Film film = makeFilm("Фильм 1", "описание", null, 120);
        Film created = filmStorage.addFilm(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setDuration(0);

        Film result = filmStorage.updateFilm(update);
        assertEquals(120, result.getDuration());
    }

    @Test
    @DisplayName("PUT не обновит пустое имя")
    void updateFilmIgnoreBlankName() {
        Film film = makeFilm("Фильм 1", "описание", null, 100);
        Film created = filmStorage.addFilm(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("");

        Film result = filmStorage.updateFilm(update);
        assertEquals("Фильм 1", result.getName());
    }

    @Test
    @DisplayName("PUT обновляет все значения")
    void updateFilmFull() {
        Film film = makeFilm("Фильм 1", "старое описание", LocalDate.of(1990, 1, 1), 50);
        Film created = filmStorage.addFilm(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("Фильм один");
        update.setDescription("новое описание");
        update.setReleaseDate(LocalDate.of(2020, 12, 31));
        update.setDuration(200);

        Film result = filmStorage.updateFilm(update);
        assertEquals("Фильм один", result.getName());
        assertEquals("новое описание", result.getDescription());
        assertEquals(LocalDate.of(2020, 12, 31), result.getReleaseDate());
        assertEquals(200, result.getDuration());
    }

    @Test
    @DisplayName("PUT не обновляет несуществующий фильм")
    void updateFilmNotFound() {
        Film update = new Film();
        update.setId(999L);
        update.setName("Фильм 1");

        assertThrows(NotFoundException.class, () -> filmStorage.updateFilm(update));
    }

    @Test
    @DisplayName("PUT не обновит значения с неверной датой")
    void updateFilmEarlyRelease() {
        Film film = makeFilm("Фильм 1", "описание", LocalDate.of(2000, 1, 1), 100);
        Film created = filmStorage.addFilm(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(ValidationException.class, () -> filmStorage.updateFilm(update));
    }

    @Test
    @DisplayName("DELETE удаление фильма")
    void deleteFilm() {
        Film film = makeFilm("Фильм 1", "описание", null, 80);
        Film created = filmStorage.addFilm(film);

        filmStorage.deleteFilm(created.getId());
        assertThrows(NotFoundException.class, () -> filmStorage.getFilmById(created.getId()));
    }

    @Test
    @DisplayName("DELETE возвращает ошибку при некорректном ID")
    void deleteFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.deleteFilm(999));
    }

    @Test
    @DisplayName("GET возвращает пустую коллекцию фильмов")
    void getAllFilmsEmpty() {
        Collection<Film> films = filmStorage.getAllFilms();
        assertTrue(films.isEmpty());
    }

    @Test
    @DisplayName("GET возвращает коллекцию всех фильмов")
    void getAllFilms() {
        filmStorage.addFilm(makeFilm("Фильм 1", "описание 1", null, 90));
        filmStorage.addFilm(makeFilm("Фильм 2", "описание 2", null, 100));
        filmStorage.addFilm(makeFilm("Фильм 3", "описание 3", null, 110));

        Collection<Film> films = filmStorage.getAllFilms();
        assertEquals(3, films.size());
    }

    @Test
    @DisplayName("DELETE очистка хранилища")
    void clearStorage() {
        filmStorage.addFilm(makeFilm("Фильм 1", "описание", null, 60));
        filmStorage.clearStorage();

        Collection<Film> films = filmStorage.getAllFilms();
        assertTrue(films.isEmpty());
    }

    @Test
    @DisplayName("Лайки фильма")
    void likesAreLoaded() {
        Film film = makeFilm("Фильм 1", "описание", null, 100);
        Film created = filmStorage.addFilm(film);

        User user1 = new User();
        user1.setEmail("practicum@yandex.ru");
        user1.setLogin("Petr");
        user1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("practicum1@yandex.ru");
        user2.setLogin("Ivan");
        user2 = userStorage.addUser(user2);

        created.getLikes().add(user1.getId());
        created.getLikes().add(user2.getId());
        filmStorage.updateFilm(created);

        Film found = filmStorage.getFilmById(created.getId());
        assertThat(found.getLikes()).containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("Жанры фильма")
    void genresAreLoaded() {
        Film film = makeFilm("Фильм 1", "описание", null, 100);
        Film created = filmStorage.addFilm(film);

        Genre comedy = new Genre();
        comedy.setId(1L);
        comedy.setName("Комедия");
        Genre drama = new Genre();
        drama.setId(2L);

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(comedy);
        genres.add(drama);
        created.setGenres(genres);
        filmStorage.updateFilm(created);

        Film found = filmStorage.getFilmById(created.getId());
        assertThat(found.getGenres()).hasSize(2);
        assertThat(found.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Рейтинг фильма")
    void ratingIsLoaded() {
        Film film = makeFilm("Фильм 1", "описание", null, 100);
        Rating rating = new Rating();
        rating.setId(1L);
        film.setMpa(rating);

        Film created = filmStorage.addFilm(film);
        Film found = filmStorage.getFilmById(created.getId());

        assertNotNull(found.getMpa());
        assertEquals(1L, found.getMpa().getId());
    }
}
