package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<Rating> {
    @Override
    public Rating mapRow(ResultSet rs, int rowNum) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getLong("rating_id"));
        rating.setName(rs.getString("code"));
        rating.setDescription(rs.getString("description"));
        return rating;
    }
}
