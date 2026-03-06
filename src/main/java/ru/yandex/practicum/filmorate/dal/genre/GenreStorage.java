package ru.yandex.practicum.filmorate.dal.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {

    Optional<Genre> findById(long genreId);

    List<Genre> findAll();

    Set<Genre> findByFilmId(long filmId);
}
