package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();


    @GetMapping
    public Collection<Film> getFilms() {
        log.info("GET /films: возвращается {} фильмов", films.size());

        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("POST /films: получен запрос на создание фильма: name='{}', description='{}', releaseDate='{}', duration={}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());

        validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм создан id={} и сохранён: {}", film.getId(), film);

        return film;
    }

    private void validateFilm(Film film) {
        log.debug("Валидация фильма: desc='{}' (длина={}), date={}, duration={}",
                film.getDescription(), film.getDescription().length(),
                film.getReleaseDate(), film.getDuration());

        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    private long getNextId() {
        long currentId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentId;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("PUT /films: запрос обновления фильма id={}", newFilm.getId());

        if (newFilm.getId() == null) {
            throw new ValidationException("Не указан id фильма");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.debug("Найден фильм для обновления: {}", oldFilm);

            if (newFilm.getName() != null) {
                validateName(newFilm.getName());

                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null) {
                validateDescription(newFilm.getDescription());

                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null) {
                validateReleaseDate(newFilm.getReleaseDate());

                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != null) {
                validateDuration(newFilm.getDuration());

                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Обновлён фильм с id = {}", newFilm.getId());

            return oldFilm;
        }

        log.warn("Фильм с id={} не найден", newFilm.getId());

        throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
    }

    public void validateName(String name) {
        if (films.values().stream().anyMatch(f -> f.getName().equals(name))) {
            throw new ValidationException("Фильм с названием " + name + " уже существует");
        }
    }

    public void validateDescription(String description) {
        if (description.length() > 200) {
            throw new ValidationException("Описание фильма превышает 200 символов");
        }
    }

    public void validateReleaseDate(String releaseDate) {
        if (LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма раньше 28 декабря 1895 года");
        }
    }

    public void validateDuration(Integer duration) {
        if (duration < 0) {
            throw new ValidationException("Продолжительность фильма есть неположительное число");
        }
    }
}
