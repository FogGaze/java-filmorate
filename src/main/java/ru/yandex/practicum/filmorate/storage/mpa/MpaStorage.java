package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

public interface MpaStorage {
    Collection<Rating> getAllRatings();

    Rating getRatingById(long id);
}
