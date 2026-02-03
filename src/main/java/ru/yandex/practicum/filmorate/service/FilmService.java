package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    public Film addLike(long userId, long filmId) {
        return filmStorage.saveLike(userId, filmId);
    }

    public void deleteLike(long userId, long filmId) {
        filmStorage.deleteLike(userId, filmId);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void deleteFilm(long filmId) {
        filmStorage.deleteFilm(filmId);
    }

    public Film getFilm(long filmId) {
        return filmStorage.getFilm(filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film createFilm(Film film) {
        validateFilm(film);

        return filmStorage.saveFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Не указан id фильма");
        }

        if (newFilm.getDescription() != null) {
            validateDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            validateReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            validateDuration(newFilm.getDuration());
        }

        return filmStorage.updateFilm(newFilm);
    }

    private void validateFilm(Film film) {
        log.debug("Валидация фильма: desc='{}' (длина={}), date={}, duration={}",
                film.getDescription(), film.getDescription().length(),
                film.getReleaseDate(), film.getDuration());

        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    private void validateDescription(String description) {
        if (description.length() > 200) {
            throw new ValidationException("Описание фильма превышает 200 символов");
        }
    }

    private void validateReleaseDate(String releaseDate) {
        if (LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма раньше 28 декабря 1895 года");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration <= 0) {
            throw new ValidationException("Продолжительность фильма есть неположительное число");
        }
    }
}
