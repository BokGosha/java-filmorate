package ru.yandex.practicum.filmorate.dal.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class DbMpaStorage extends BaseRepository<Mpa> implements MpaStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpas";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpas WHERE id = ?";

    public DbMpaStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Mpa> findById(long genreId) {
        return findOne(FIND_BY_ID_QUERY, genreId);
    }

    @Override
    public List<Mpa> findAll() {
        return findMany(FIND_ALL_QUERY);
    }
}
