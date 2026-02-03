package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceValidationTest {

    private FilmService service;

    @BeforeEach
    void setUp() {
        service = new FilmService(new InMemoryFilmStorage(new InMemoryUserStorage()));
    }

    @Test
    void validateDescription_length200_ok() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(100);
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> service.createFilm(film));
    }

    @Test
    void validateDescription_length201_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(100);
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createFilm(film));
        assertEquals("Описание фильма превышает 200 символов", exception.getMessage());
    }

    @Test
    void validateDuration_negativeNumber_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(-1);
        film.setDescription("a".repeat(200));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createFilm(film));
        assertEquals("Продолжительность фильма есть неположительное число", exception.getMessage());
    }

    @Test
    void validateDuration_zeroNumber_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(0);
        film.setDescription("a".repeat(200));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createFilm(film));
        assertEquals("Продолжительность фильма есть неположительное число", exception.getMessage());
    }

    @Test
    void validateDuration_positiveNumber_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(1);
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> service.createFilm(film));
    }

    @Test
    void validateReleaseDate_before1895_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("1894-09-01");
        film.setDuration(1);
        film.setDescription("a".repeat(200));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createFilm(film));
        assertEquals("Дата релиза фильма раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void validateReleaseDate_after1895_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("1896-09-01");
        film.setDuration(1);
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> service.createFilm(film));
    }
}
