package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.user.LikeDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dal.film.FilmStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final UserService userService;

    public FilmDto addLike(long userId, long filmId) {
        filmStorage.saveLike(userId, filmId);

        Film film = getFilmById(filmId);

        setMpaGenresLikes(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public void deleteLike(long userId, long filmId) {
        filmStorage.deleteLike(userId, filmId);
    }

    public List<FilmDto> getFilms() {
        List<Film> films = filmStorage.findAll();

        setMpaGenresAndLikesForFilms(films);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto getFilm(long filmId) {
        Film film = getFilmById(filmId);

        setMpaGenresLikes(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getPopularFilms(int count) {
        List<Film> films = filmStorage.findPopular(count);

        setMpaGenresAndLikesForFilms(films);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private void setMpaGenresAndLikesForFilms(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).toList();

        Map<Long, Set<Genre>> genresByFilmIds =
                filmStorage.findGenresForFilmIds(filmIds);
        Map<Long, Mpa> mpaByFilmIds =
                filmStorage.findMpaForFilmIds(filmIds);
        Map<Long, Set<LikeDto>> likesByFilmIds =
                userService.getAllLikesByFilmIds(filmIds);

        films.forEach(film -> {
                    film.setGenres(genresByFilmIds.get(film.getId()));
                    film.setMpa(mpaByFilmIds.get(film.getId()));
                    film.setLikes(likesByFilmIds.get(film.getId()));
                }
        );
    }

    public FilmDto createFilm(NewFilmRequest request) {
        validateFilm(request);

        Film film = FilmMapper.mapToFilm(request);

        if (request.hasMpa()) {
            Mpa mpa = mpaService.getMpa(request.getMpa().getId());
            film.setMpa(mpa);
        }

        Set<Genre> requestGenres = request.getGenres();
        if (requestGenres != null) {
            List<Long> genreIds = requestGenres.stream().map(Genre::getId).toList();
            for (Long genreId : genreIds) {
                if (genreId < 1 ||  genreId > 6) {
                    log.warn("Жанр с id={} не найден", genreId);
                    throw new NotFoundException("Жанр с id=" + genreId + " не найден");
                }
            }
            Set<Genre> genres = genreService.getGenresByIds(genreIds);
            film.setGenres(genres);
        }

        film = filmStorage.save(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        Film updatedFilm = FilmMapper.updateFilmFields(getFilmById(request.getId()), request);

        setMpaGenresLikes(updatedFilm);

        updatedFilm = filmStorage.update(updatedFilm);

        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    private void validateFilm(NewFilmRequest request) {
        log.debug("Валидация фильма: desc='{}' (длина={}), date={}, duration={}",
                request.getDescription(), request.getDescription().length(),
                request.getReleaseDate(), request.getDuration());

        validateName(request.getName());
        validateDescription(request.getDescription());
        validateReleaseDate(request.getReleaseDate());
        validateDuration(request.getDuration());
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Описание фильма превышает 200 символов");
        }
    }

    private void validateDescription(String description) {
        if (description.length() > 200) {
            throw new ValidationException("Описание фильма превышает 200 символов");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма раньше 28 декабря 1895 года");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration <= 0) {
            throw new ValidationException("Продолжительность фильма есть неположительное число");
        }
    }

    private void setMpaGenresLikes(Film film) {
        if (film.getMpa() != null) {
            Mpa mpa = mpaService.getMpa(film.getMpa().getId());
            film.setMpa(mpa);
        }

        Set<Genre> genres = genreService.getGenresByFilmId(film.getId())
                .stream()
                .sorted(Comparator.comparingLong(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(genres);

        Set<LikeDto> likes = userService.getAllLikesByFilmId(film.getId())
                .stream()
                .map(UserMapper::mapToLikeDto)
                .sorted(Comparator.comparingLong(LikeDto::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setLikes(likes);
    }

    private Film getFilmById(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", filmId);
                    return new NotFoundException("Фильм с id=" + filmId + " не найден");
                });
    }
}
