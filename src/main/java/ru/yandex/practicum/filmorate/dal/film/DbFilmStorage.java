package ru.yandex.practicum.filmorate.dal.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@Repository
public class DbFilmStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL =
            "SELECT * " +
                    "FROM films";
    private static final String FIND_POPULAR =
            "SELECT f.* " +
                    "FROM films f " +
                    "JOIN (" +
                    "   SELECT film_id, COUNT(*) AS likes_count " +
                    "   FROM film_likes " +
                    "   GROUP BY film_id " +
                    "   ORDER BY likes_count DESC " +
                    "   LIMIT ?" +
                    ") fl ON f.id = fl.film_id " +
                    "ORDER BY fl.likes_count DESC";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * " +
                    "FROM films " +
                    "WHERE id = ?";
    private static final String INSERT_WITH_MPA_QUERY =
            "INSERT INTO films (name, description," +
            "release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_WITHOUT_MPA_QUERY =
            "INSERT INTO films (name, description," +
            "release_date, duration) VALUES (?, ?, ?, ?)";
    private static final String INSERT_GENRE_FILM_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?," +
            "duration = ?, mpa_id = ? " +
                    "WHERE id = ?";
    private static final String DELETE_QUERY =
            "DELETE films " +
                    "WHERE id = ?";
    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY =
            "DELETE film_likes " +
                    "WHERE film_id = ?";
    private static final String FIND_GENRES_FOR_FILM_IDS_QUERY =
            "SELECT fg.film_id, g.* " +
                    "FROM film_genres fg " +
                    "JOIN genres g ON g.id = fg.genre_id " +
                    "WHERE fg.film_id IN (%s)";
    private static final String FIND_MPA_FOR_FILM_IDS_QUERY =
            "SELECT f.id AS film_id, m.* " +
                    "FROM films f " +
                    "JOIN mpas m ON m.id = f.mpa_id " +
                    "WHERE f.id IN (%s)";

    public DbFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Map<Long, Set<Genre>> findGenresForFilmIds(List<Long> filmIds) {
        String placeholders = setPlaceholders(filmIds.size());
        String sql = FIND_GENRES_FOR_FILM_IDS_QUERY.formatted(placeholders);

        return jdbc.query(sql, rs -> {
            Map<Long, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                result.computeIfAbsent(filmId, id -> new HashSet<>()).add(genre);
            }
            return result;
        }, filmIds.toArray());
    }

    @Override
    public Map<Long, Mpa> findMpaForFilmIds(List<Long> filmIds) {
        String placeholders = setPlaceholders(filmIds.size());
        String sql = FIND_MPA_FOR_FILM_IDS_QUERY.formatted(placeholders);

        return jdbc.query(sql, rs -> {
            Map<Long, Mpa> result = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("id"));
                mpa.setName(rs.getString("name"));
                result.put(filmId, mpa);
            }
            return result;
        },  filmIds.toArray());
    }

    private String setPlaceholders(int size) {
        return String.join(", ", Collections.nCopies(size, "?"));
    }

    @Override
    public List<Film> findPopular(int count) {
        return findMany(FIND_POPULAR, count);
    }

    @Override
    public void deleteLike(long userId, long filmId) {
        delete(DELETE_LIKE_QUERY, filmId);
        log.info("Лайк пользователя с id={} фильму с id={} удалён", userId, filmId);
    }

    @Override
    public void saveLike(long userId, long filmId) {
        update(INSERT_LIKE_QUERY, filmId, userId);
        log.info("Лайк пользователя с id={} фильму с id={} создан и сохранён", userId, filmId);
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public Optional<Film> findById(long filmId) {
        log.info("Фильм с id={} получен", filmId);
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    @Override
    public Film save(Film film) {
        long id;

        if (film.getMpa() != null) {
            id = insert(
                    INSERT_WITH_MPA_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId()
            );
        } else {
            id = insert(
                    INSERT_WITHOUT_MPA_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration()
            );
        }

        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            jdbc.batchUpdate(
                    INSERT_GENRE_FILM_QUERY,
                    film.getGenres(),
                    film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genre.getId());
                    }
            );
        }

        log.info("Фильм создан id={} и сохранён: {}", film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        log.info("Фильм с id={} обновлён", film.getId());
        return film;
    }

    @Override
    public void delete(long filmId) {
        delete(DELETE_QUERY, filmId);
        log.info("Фильм с id={} удалён", filmId);
    }
}
