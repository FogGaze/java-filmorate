package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaRowMapper mpaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public Collection<Rating> getAllRatings() {
        String sql = "SELECT rating_id, code, description FROM mpa_ratings ORDER BY rating_id";
        List<Rating> ratings = jdbcTemplate.query(sql, mpaRowMapper);
        log.trace("Передан список всех рейтингов");
        return ratings;
    }

    @Override
    public Rating getRatingById(long id) {
        String sql = "SELECT rating_id, code, description FROM mpa_ratings WHERE rating_id=?";
        Rating rating = jdbcTemplate.query(sql, mpaRowMapper, id).stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Передан некорректный ID рейтинга {}", id);
                    return new NotFoundException("Рейтинг с id = " + id + " не найден");
                });
        log.trace("Передан рейтинг с ID {}", id);
        return rating;
    }
}
