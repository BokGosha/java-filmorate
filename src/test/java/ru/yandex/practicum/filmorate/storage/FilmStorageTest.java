package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilmStorageTest {

    private InMemoryFilmStorage filmStorage;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = mock(UserStorage.class);
        when(userStorage.getUser(anyLong())).thenReturn(null);
        filmStorage = new InMemoryFilmStorage(userStorage);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("desc");
        film.setDuration(120);
        film.setReleaseDate("2000-01-01");
        return film;
    }

    @Test
    void saveFilm_shouldAssignIdAndStore() {
        Film film = createFilm("Film 1");

        Film saved = filmStorage.saveFilm(film);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals(saved, filmStorage.getFilm(saved.getId()));
    }

    @Test
    void getFilm_notExisting_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getFilm(999L));
    }

    @Test
    void updateFilm_existing_shouldUpdate() {
        Film film = createFilm("Film 1");
        Film saved = filmStorage.saveFilm(film);

        Film updated = createFilm("New name");
        updated.setId(saved.getId());

        Film result = filmStorage.updateFilm(updated);

        assertEquals("New name", filmStorage.getFilm(saved.getId()).getName());
        assertEquals("New name", result.getName());
    }

    @Test
    void updateFilm_notExisting_shouldThrowNotFound() {
        Film film = createFilm("Film 1");
        film.setId(100L);

        assertThrows(NotFoundException.class, () -> filmStorage.updateFilm(film));
    }

    @Test
    void deleteFilm_existing_shouldRemove() {
        Film film = createFilm("Film 1");
        Film saved = filmStorage.saveFilm(film);

        filmStorage.deleteFilm(saved.getId());

        assertThrows(NotFoundException.class, () -> filmStorage.getFilm(saved.getId()));
    }

    @Test
    void deleteFilm_notExisting_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.deleteFilm(999L));
    }

    @Test
    void saveLike_shouldAddLikeAndUseUserStorage() {
        Film film = createFilm("Film 1");
        Film saved = filmStorage.saveFilm(film);

        Film result = filmStorage.saveLike(1L, saved.getId());

        assertTrue(result.getLikes().contains(1L));
        assertTrue(filmStorage.getFilm(saved.getId()).getLikes().contains(1L));
        verify(userStorage, times(1)).getUser(1L);
    }

    @Test
    void saveLike_notExistingFilm_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.saveLike(1L, 999L));
    }

    @Test
    void deleteLike_shouldRemoveLike() {
        Film film = createFilm("Film 1");
        Film saved = filmStorage.saveFilm(film);

        filmStorage.saveLike(1L, saved.getId());
        filmStorage.saveLike(2L, saved.getId());

        filmStorage.deleteLike(1L, saved.getId());

        Set<Long> likes = filmStorage.getFilm(saved.getId()).getLikes();
        assertFalse(likes.contains(1L));
        assertTrue(likes.contains(2L));
    }

    @Test
    void deleteLike_notExistingFilm_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.deleteLike(1L, 999L));
    }

    @Test
    void getPopularFilms_shouldReturnSortedByLikesAndLimited() {
        Film film1 = createFilm("Film 1");
        Film film2 = createFilm("Film 2");
        Film film3 = createFilm("Film 3");

        Film f1 = filmStorage.saveFilm(film1);
        Film f2 = filmStorage.saveFilm(film2);
        Film f3 = filmStorage.saveFilm(film3);

        filmStorage.saveLike(1L, f1.getId());

        filmStorage.saveLike(1L, f2.getId());
        filmStorage.saveLike(2L, f2.getId());
        filmStorage.saveLike(3L, f2.getId());

        filmStorage.saveLike(1L, f3.getId());
        filmStorage.saveLike(2L, f3.getId());

        List<Film> popular = filmStorage.getPopularFilms(10);

        assertEquals(3, popular.size());
        assertEquals(f2.getId(), popular.get(0).getId());
        assertEquals(f3.getId(), popular.get(1).getId());
        assertEquals(f1.getId(), popular.get(2).getId());
    }

    @Test
    void getPopularFilms_shouldLimitToCountButNotMoreThan10() {
        for (int i = 0; i < 12; i++) {
            filmStorage.saveFilm(createFilm("Film " + i));
        }

        List<Film> five = filmStorage.getPopularFilms(5);
        assertEquals(5, five.size());

        List<Film> twenty = filmStorage.getPopularFilms(20);
        assertEquals(10, twenty.size());
    }

    @Test
    void getFilms_shouldReturnAllFilms() {
        assertTrue(filmStorage.getFilms().isEmpty());

        filmStorage.saveFilm(createFilm("Film 1"));
        filmStorage.saveFilm(createFilm("Film 2"));

        assertEquals(2, filmStorage.getFilms().size());
    }
}
