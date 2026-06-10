package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String sql = "SELECT genre_id, name, description FROM genres ORDER BY genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper);
        log.trace("Передан список всех жанров");
        return genres;
    }

    @Override
    public Genre getGenreById(long id) {
        String sql = "SELECT genre_id, name, description FROM genres WHERE genre_id=?";
        Genre genre = jdbcTemplate.query(sql, genreRowMapper, id).stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Передан некорректный ID жанра {}", id);
                    return new NotFoundException("Жанр с id = " + id + " не найден");
                });
        log.trace("Передан жанр с ID {}", id);
        return genre;
    }
}
