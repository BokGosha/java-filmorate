package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.genre.GenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

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
}
