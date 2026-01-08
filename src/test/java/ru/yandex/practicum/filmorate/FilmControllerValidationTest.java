package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.validateDescription(film.getDescription()));
    }

    @Test
    void validateDescription_length201_throws() {
        Film film = new Film();
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.validateDescription(film.getDescription()));
        assertEquals("Описание фильма превышает 200 символов", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1895-12-28", "1900-01-01", "2023-12-31"})
    void validateReleaseDate_ok(String validDate) {
        assertDoesNotThrow(() -> controller.validateReleaseDate(validDate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1895-12-27", "1800-01-01"})
    void validateReleaseDate_tooEarly_throws(String tooEarly) {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.validateReleaseDate(tooEarly));
        assertEquals("Дата релиза фильма раньше 28 декабря 1895 года", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "120, true",
            "-1, false"
    })
    void validateDuration(Integer duration, boolean expectedValid) {
        if (expectedValid) {
            assertDoesNotThrow(() -> controller.validateDuration(duration));
        } else {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> controller.validateDuration(duration));
            assertEquals("Продолжительность фильма есть неположительное число", exception.getMessage());
        }
    }
}
