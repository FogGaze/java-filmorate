package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    @NotNull(groups = OnUpdate.class, message = "ID обязателен при обновлении")
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Название не может быть пустым")
    private String name;

    @Size(groups = {OnCreate.class, OnUpdate.class}, max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(groups = OnCreate.class, message = "Продолжительность должна быть положительной")
    private int duration;

    private Set<Long> likes = new HashSet<>();
}
