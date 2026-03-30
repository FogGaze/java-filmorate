package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.OnCreate;
import ru.yandex.practicum.filmorate.model.OnUpdate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        log.trace("Получен запрос коллекции всех фильмов");
        log.trace("Передана коллекция всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Validated(OnCreate.class) @RequestBody Film film) {
        log.trace("Получен запрос на добавление нового фильма");

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            log.warn("Передано некорректное значение релиза фильма {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        film.setId(getNextId());
        log.trace("Установлено ID {} для фильма {}", film.getId(), film.getName());

        films.put(film.getId(), film);
        log.trace("Фильм {} с ID {} добавлен в базу данных", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Validated(OnUpdate.class) @RequestBody Film newFilm) {
        log.trace("Получен запрос на обновление существующего фильма");

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Передано некорректное значение ID фильма {}", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

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
            if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
                log.warn("Передано некорректное значение релиза фильма {}", newFilm.getReleaseDate());
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            } else {
                log.trace("Релиз фильма {} изменен", oldFilm.getName());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
        }

        if (newFilm.getDuration() != null) {
            log.trace("Продолжительность фильма {} изменена", oldFilm.getName());
            oldFilm.setDuration(newFilm.getDuration());
        }

        films.put(oldFilm.getId(), oldFilm);
        log.trace("Фильм {}, ID {} обновлён в базе данных", oldFilm.getName(), oldFilm.getId());
        return oldFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
