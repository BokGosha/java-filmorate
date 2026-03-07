package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.genre.GenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public Genre getGenre(long genreId) {
        return genreStorage.findById(genreId)
                .orElseThrow(() -> {
                    log.warn("Жанр с id={} не найден", genreId);
                    return new NotFoundException("Жанр с id=" + genreId + " не найден");
                });
    }

    public List<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public Set<Genre> getGenresByIds(List<Long> genreIds) {
        return genreStorage.findGenresByIds(genreIds)
                .stream()
                .sorted(Comparator.comparingLong(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Genre> getGenresByFilmId(long filmId) {
        return genreStorage.findByFilmId(filmId);
    }
}
