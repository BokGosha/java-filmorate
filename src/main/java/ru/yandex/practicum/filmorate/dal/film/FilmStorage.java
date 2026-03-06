package ru.yandex.practicum.filmorate.dal.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    List<Film> findPopular(int count);

    List<Film> findAll();

    void deleteLike(long userId, long filmId);

    void saveLike(long userId, long filmId);

    Optional<Film> findById(long filmId);

    Film save(Film film);

    Film update(Film film);

    void delete(long filmId);
}
