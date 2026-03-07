package ru.yandex.practicum.filmorate.dal.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    List<Film> findPopular(int count);

    List<Film> findAll();

    void deleteLike(long userId, long filmId);

    void saveLike(long userId, long filmId);

    Optional<Film> findById(long filmId);

    Film save(Film film);

    Film update(Film film);

    void delete(long filmId);

    Map<Long, Mpa> findMpaForFilmIds(List<Long> filmIds);

    Map<Long, Set<Genre>> findGenresForFilmIds(List<Long> filmIds);
}
