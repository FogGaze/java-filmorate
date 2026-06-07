package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public Film addFilm(Film film) {
        checkEarlyRelease(film.getReleaseDate());
        checkRatingExists(film.getMpa());
        checkGenresExist(film.getGenres());

        String sql = "INSERT INTO film (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        syncGenres(film);
        log.trace("Фильм {} с ID {} добавлен в базу данных", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        Film oldFilm = getFilmById(newFilm.getId());

        if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
            log.trace("Название фильма {} изменено на {}", oldFilm.getName(), newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            log.trace("Описание фильма {} изменено", oldFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            checkEarlyRelease(newFilm.getReleaseDate());
            log.trace("Релиз фильма {} изменен", oldFilm.getName());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() > 0) {
            log.trace("Продолжительность фильма {} изменена", oldFilm.getName());
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getLikes() != null) {
            log.trace("Список лайков фильма c ID {} обновлен", oldFilm.getId());
            oldFilm.setLikes(newFilm.getLikes());
        }
        if (newFilm.getGenres() != null) {
            checkGenresExist(newFilm.getGenres());
            oldFilm.setGenres(newFilm.getGenres());
        }
        if (newFilm.getMpa() != null) {
            checkRatingExists(newFilm.getMpa());
            oldFilm.setMpa(newFilm.getMpa());
        }

        String sql = "UPDATE film SET name=?, description=?, release_date=?, duration=?, rating_id=? WHERE film_id=?";
        jdbcTemplate.update(sql,
                oldFilm.getName(),
                oldFilm.getDescription(),
                oldFilm.getReleaseDate() != null ? Date.valueOf(oldFilm.getReleaseDate()) : null,
                oldFilm.getDuration(),
                oldFilm.getMpa() != null ? oldFilm.getMpa().getId() : null,
                oldFilm.getId());

        syncLikes(oldFilm);
        syncGenres(oldFilm);
        log.trace("Фильм {}, ID {} обновлён в базе данных", oldFilm.getName(), oldFilm.getId());
        return oldFilm;
    }

    @Override
    public void deleteFilm(long id) {
        int deleted = jdbcTemplate.update("DELETE FROM film WHERE film_id=?", id);
        if (deleted == 0) {
            log.warn("Передано некорректное значение ID фильма {}", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.trace("Фильм с ID {} удалён из хранилища", id);
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT f.*, r.code, r.description AS rating_description " +
                "FROM film f LEFT JOIN mpa_ratings r ON f.rating_id = r.rating_id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        for (Film film : films) {
            loadLikes(film);
            loadGenres(film);
        }
        log.trace("Передана коллекция всех фильмов");
        return films;
    }

    @Override
    public Film getFilmById(long id) {
        String sql = "SELECT f.*, r.code, r.description AS rating_description " +
                "FROM film f LEFT JOIN mpa_ratings r ON f.rating_id = r.rating_id WHERE f.film_id=?";
        Film film = jdbcTemplate.query(sql, filmRowMapper, id).stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Передано некорректное значение ID фильма {}", id);
                    return new NotFoundException("Фильм с id = " + id + " не найден");
                });
        loadLikes(film);
        loadGenres(film);
        log.trace("Передан фильм с ID {}", id);
        return film;
    }

    @Override
    public void clearStorage() {
        jdbcTemplate.update("DELETE FROM film");
        log.debug("Таблица film очищена");
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id=?";
        List<Long> userIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), film.getId());
        film.setLikes(new HashSet<>(userIds));
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name, g.description " +
                "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id=?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("name"));
            genre.setDescription(rs.getString("description"));
            return genre;
        }, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void syncLikes(Film film) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id=?", film.getId());
        if (film.getLikes() != null) {
            String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            for (Long userId : film.getLikes()) {
                jdbcTemplate.update(sql, film.getId(), userId);
            }
        }
    }

    private void syncGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id=?", film.getId());
        if (film.getGenres() != null) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }
    }

    private void checkEarlyRelease(LocalDate release) {
        if (release != null && release.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Передано некорректное значение релиза фильма {}", release);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void checkRatingExists(Rating rating) {
        if (rating == null) return;
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE rating_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, rating.getId());
        if (count == 0) {
            log.warn("Передан некорректный ID рейтинга {}", rating.getId());
            throw new NotFoundException("Рейтинг с id = " + rating.getId() + " не найден");
        }
    }

    private void checkGenresExist(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;
        for (Genre genre : genres) {
            String sql = "SELECT COUNT(*) FROM genres WHERE genre_id=?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genre.getId());
            if (count == 0) {
                log.warn("Передан некорректный ID жанра {}", genre.getId());
                throw new NotFoundException("Жанр с id = " + genre.getId() + " не найден");
            }
        }
    }
}
