package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final UserStorage userStorage;

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int countLikes1 = f1.getLikes().size();
                    int countLikes2 = f2.getLikes().size();

                    return countLikes2 - countLikes1;
                })
                .limit(Math.min(count, 10))
                .toList();
    }

    @Override
    public void deleteLike(long userId, long filmId) {
        containsFilm(filmId);
        userStorage.getUser(userId);

        films.get(filmId).getLikes().remove(userId);
        log.info("Лайк пользователя с id={} фильму с id={} удалён", userId, filmId);
    }

    @Override
    public Film saveLike(long userId, long filmId) {
        containsFilm(filmId);
        userStorage.getUser(userId);

        films.get(filmId).getLikes().add(userId);
        log.info("Лайк пользователя с id={} фильму с id={} создан и сохранён", userId, filmId);
        return films.get(filmId);
    }

    @Override
    public Film getFilm(long filmId) {
        containsFilm(filmId);

        log.info("Фильм с id={} получен", filmId);
        return films.get(filmId);
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film saveFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан id={} и сохранён: {}", film.getId(), film);
        return film;
    }

    private long getNextId() {
        long currentId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentId;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        containsFilm(newFilm.getId());

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id={} обновлён", newFilm.getId());
        return newFilm;
    }

    @Override
    public void deleteFilm(long filmId) {
        containsFilm(filmId);

        films.remove(filmId);
        log.info("Фильм с id={} удалён", filmId);
    }

    private void containsFilm(long filmId) {
        if (!films.containsKey(filmId)) {
            log.warn("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }
}
