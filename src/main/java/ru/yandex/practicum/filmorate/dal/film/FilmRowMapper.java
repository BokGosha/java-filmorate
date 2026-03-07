package ru.yandex.practicum.filmorate.dal.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(resultSet.getLong("id"));
        film.setDuration(resultSet.getInt("duration"));
        film.setReleaseDate(resultSet.getObject("release_date", LocalDate.class));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));

        Mpa mpa = new Mpa();
        mpa.setId(resultSet.getLong("mpa_id"));

        if (mpa.getId() != 0) {
            film.setMpa(mpa);
        }

        return film;
    }
}
