package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        LocalDate releaseDate = rs.getDate("release_date") != null
                ? rs.getDate("release_date").toLocalDate()
                : null;
        film.setReleaseDate(releaseDate);
        film.setDuration(rs.getInt("duration"));

        long ratingId = rs.getLong("rating_id");
        if (!rs.wasNull()) {
            Rating rating = new Rating();
            rating.setId(ratingId);
            rating.setName(rs.getString("code"));
            rating.setDescription(rs.getString("rating_description"));
            film.setMpa(rating);
        }

        return film;
    }
}
