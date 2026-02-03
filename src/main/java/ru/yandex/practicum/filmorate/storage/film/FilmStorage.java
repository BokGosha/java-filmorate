package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    List<Film> getPopularFilms(int count);

    void deleteLike(long userId, long filmId);

    Film saveLike(long userId, long filmId);

    Film getFilm(long filmId);

    Collection<Film> getFilms();

    Film saveFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(long filmId);
}
