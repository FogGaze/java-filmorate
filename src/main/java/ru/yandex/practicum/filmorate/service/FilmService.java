package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(long filmId, long userId) {
        User user = getUserById(userId);
        Film film = getFilmById(filmId);

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь с ID {} уже поставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);

        log.trace("Фильму с ID {} поставлен лайк пользователем {}", filmId, userId);
        updateFilm(film);
    }

    public void removeLike(long filmId, long userId) {
        User user = getUserById(userId);
        Film film = getFilmById(filmId);

        if (!film.getLikes().contains(userId)) {
            log.warn("Пользователь с ID {} не ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);

        log.trace("Фильму с ID {} удален лайк пользователя {}", filmId, userId);
        updateFilm(film);
    }

    public List<Film> getPopularFilms(int count) {

        if (count <= 0) {
            count = 10;
        }

        List<Film> popularFilms = getAllFilms().stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());

        log.trace("Передан список популярных фильмов длиной {}", count);
        return popularFilms;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    private User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
    }

    public void clearStorage() {
        filmStorage.clearStorage();
    }
}
