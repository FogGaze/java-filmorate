package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public Collection<Rating> getRatings() {
        log.trace("Получен запрос списка всех рейтингов");
        return mpaService.getAllRatings();
    }

    @GetMapping("/{id}")
    public Rating getRatingById(@PathVariable @Positive long id) {
        log.trace("Получен запрос рейтинга по id={}", id);
        return mpaService.getRatingById(id);
    }
}
