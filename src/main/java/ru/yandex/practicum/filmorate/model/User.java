package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    @NotNull(groups = OnUpdate.class, message = "ID обязателен при обновлении")
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Email не может быть пустым")
    @Email(groups = {OnCreate.class, OnUpdate.class}, message = "Некорректный email")
    private String email;

    @NotBlank(groups = OnCreate.class, message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", groups = {OnCreate.class, OnUpdate.class}, message = "Логин не должен содержать пробелов")
    private String login;

    private String name;

    @PastOrPresent(groups = {OnCreate.class, OnUpdate.class}, message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
