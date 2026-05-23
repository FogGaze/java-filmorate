package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {

        checkEarlyRelease(film.getReleaseDate());

        film.setId(getNextId());
        log.trace("Установлено ID {} для фильма {}", film.getId(), film.getName());

        films.put(film.getId(), film);
        log.trace("Фильм {} с ID {} добавлен в базу данных", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {

        checkIdFilm(newFilm.getId());

        Film oldFilm = films.get(newFilm.getId());

        if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
            log.trace("Название фильма {} изменено на {}", oldFilm.getName(), newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            log.trace("Описание фильма {} изменено", oldFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            checkEarlyRelease(newFilm.getReleaseDate());
            log.trace("Релиз фильма {} изменен", oldFilm.getName());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() > 0) {
            log.trace("Продолжительность фильма {} изменена", oldFilm.getName());
            oldFilm.setDuration(newFilm.getDuration());
        }

        if (newFilm.getLikes() != null) {
            log.trace("Список лайков фильма c ID {} обновлен", oldFilm.getId());
            oldFilm.setLikes(newFilm.getLikes());
        }

        films.put(oldFilm.getId(), oldFilm);
        log.trace("Фильм {}, ID {} обновлён в базе данных", oldFilm.getName(), oldFilm.getId());
        return oldFilm;
    }

    @Override
    public void deleteFilm(long id) {

        checkIdFilm(id);

        Film removed = films.get(id);
        films.remove(id);
        log.trace("Фильм {} с ID {} удалён из хранилища", removed.getName(), removed.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.trace("Передана коллекция всех фильмов");
        return films.values();
    }

    @Override
    public Film getFilmById(long id) {
        checkIdFilm(id);
        log.trace("Передан фильм с ID {}", id);
        return films.get(id);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkIdFilm(long id) {
        if (!films.containsKey(id)) {
            log.warn("Передано некорректное значение ID фильма {}", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    private void checkEarlyRelease(LocalDate release) {
        if (release != null && release.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Передано некорректное значение релиза фильма {}", release);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    @Override
    public void clearStorage() {
        films.clear();
        log.debug("Хранилище хэш мап очищено");
    }
}
