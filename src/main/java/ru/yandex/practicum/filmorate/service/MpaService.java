package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Rating> getAllRatings() {
        return mpaStorage.getAllRatings();
    }

    public Rating getRatingById(long id) {
        return mpaStorage.getRatingById(id);
    }
}
