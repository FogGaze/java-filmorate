package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public Collection<Genre> getGenres() {
        log.trace("Получен запрос списка всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable @Positive long id) {
        log.trace("Получен запрос жанра по id={}", id);
        return genreService.getGenreById(id);
    }
}
