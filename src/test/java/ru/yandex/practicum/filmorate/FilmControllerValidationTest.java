package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerValidationTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void validateDescription_length200_ok() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(100);
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.createFilm(film));
    }

    @Test
    void validateDescription_length201_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(100);
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createFilm(film));
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
                () -> controller.createFilm(film));
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
                () -> controller.createFilm(film));
        assertEquals("Продолжительность фильма есть неположительное число", exception.getMessage());
    }

    @Test
    void validateDuration_positiveNumber_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("2026-09-01");
        film.setDuration(1);
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.createFilm(film));
    }

    @Test
    void validateReleaseDate_before1895_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("1894-09-01");
        film.setDuration(1);
        film.setDescription("a".repeat(200));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createFilm(film));
        assertEquals("Дата релиза фильма раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void validateReleaseDate_after1895_throws() {
        Film film = new Film();
        film.setName("film");
        film.setReleaseDate("1896-09-01");
        film.setDuration(1);
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.createFilm(film));
    }
}
