package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.OnCreate;
import ru.yandex.practicum.filmorate.model.OnUpdate;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        log.trace("Получен запрос коллекции всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive long id) {
        log.trace("Получен запрос фильма по id={}", id);
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.trace("Получен запрос списка популярных фильмов длиной {}", count);
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    public Film addFilm(@Validated(OnCreate.class) @RequestBody Film film) {
        log.trace("Получен запрос на добавление нового фильма");
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated(OnUpdate.class) @RequestBody Film newFilm) {
        log.trace("Получен запрос на обновление существующего фильма");
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        log.trace("Получен запрос пользователя {} поставить лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable @Positive long id) {
        log.trace("Получен запрос на удаление фильма с id={}", id);
        filmService.deleteFilm(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        log.trace("Получен запрос пользователя {} удалить лайк фильму {}", userId, id);
        filmService.removeLike(id, userId);
    }

    public void clearStorage() {
        filmService.clearStorage();
    }
}
