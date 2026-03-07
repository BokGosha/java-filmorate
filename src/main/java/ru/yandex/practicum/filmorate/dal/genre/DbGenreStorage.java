package ru.yandex.practicum.filmorate.dal.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Repository
public class DbGenreStorage extends BaseRepository<Genre> implements GenreStorage {

    private static final String FIND_ALL_QUERY =
            "SELECT * " +
                    "FROM genres";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * " +
                    "FROM genres " +
                    "WHERE id = ?";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY =
            "SELECT * " +
                    "FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ?";
    private static final String FIND_GENRES_BY_IDS_QUERY =
            "SELECT * " +
                    "FROM genres " +
                    "WHERE id IN (%s)";

    public DbGenreStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Genre> findById(long genreId) {
        return findOne(FIND_BY_ID_QUERY, genreId);
    }

    @Override
    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Set<Genre> findByFilmId(long filmId) {
        return new HashSet<>(findMany(FIND_GENRES_BY_FILM_ID_QUERY, filmId));
    }

    @Override
    public Set<Genre> findGenresByIds(List<Long> genreIds) {
        String placeholders = setPlaceholders(genreIds.size());
        String sql = FIND_GENRES_BY_IDS_QUERY.formatted(placeholders);

        return new HashSet<>(findMany(sql, genreIds.toArray()));
    }

    private String setPlaceholders(int size) {
        return String.join(", ", Collections.nCopies(size, "?"));
    }
}
