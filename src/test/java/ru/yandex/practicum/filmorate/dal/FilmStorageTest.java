package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        DbFilmStorage.class,
        DbUserStorage.class,
        FilmRowMapper.class,
        UserRowMapper.class
})
class FilmStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    @Test
    @DisplayName("Должен возвращать пустой Optional, если фильм не найден")
    void shouldReturnEmptyWhenFilmNotFound() {
        Optional<Film> filmOptional = filmStorage.findById(9999L);

        assertThat(filmOptional).isEmpty();
    }

    @Test
    @DisplayName("Должен сохранять фильм")
    void shouldSaveFilm() {
        Film film = makeFilm(
                "Interstellar",
                "Space travel",
                LocalDate.of(2014, 11, 7),
                169,
                3L
        );

        Film savedFilm = filmStorage.save(film);

        assertThat(savedFilm.getId()).isPositive();

        Optional<Film> filmOptional = filmStorage.findById(savedFilm.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getName()).isEqualTo("Interstellar");
                    assertThat(found.getDescription()).isEqualTo("Space travel");
                    assertThat(found.getDuration()).isEqualTo(169);
                    assertThat(found.getReleaseDate()).isEqualTo(LocalDate.of(2014, 11, 7));
                });
    }

    @Test
    @DisplayName("Должен находить фильм по id")
    void shouldFindFilmById() {
        Film newFilm = makeFilm(
                "Old name",
                "Old description",
                LocalDate.of(2000, 1, 1),
                100,
                2L
        );
        Film savedFilm = filmStorage.save(newFilm);

        Optional<Film> filmOptional = filmStorage.findById(11L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(11L);
                    assertThat(film.getName()).isNotBlank();
                });
    }

    @Test
    @DisplayName("Должен обновлять фильм")
    void shouldUpdateFilm() {
        Film film = makeFilm(
                "Old name",
                "Old description",
                LocalDate.of(2000, 1, 1),
                100,
                2L
        );
        Film savedFilm = filmStorage.save(film);

        savedFilm.setName("New name");
        savedFilm.setDescription("New description");
        savedFilm.setDuration(120);
        savedFilm.setReleaseDate(LocalDate.of(2001, 2, 2));

        Film updatedFilm = filmStorage.update(savedFilm);

        assertThat(updatedFilm.getId()).isEqualTo(savedFilm.getId());

        Optional<Film> filmOptional = filmStorage.findById(savedFilm.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getName()).isEqualTo("New name");
                    assertThat(found.getDescription()).isEqualTo("New description");
                    assertThat(found.getDuration()).isEqualTo(120);
                    assertThat(found.getReleaseDate()).isEqualTo(LocalDate.of(2001, 2, 2));
                });
    }

    @Test
    @DisplayName("Должен возвращать список фильмов")
    void shouldFindAllFilms() {
        Film newFilm1 = makeFilm(
                "Name 1",
                "Description 1",
                LocalDate.of(2000, 1, 1),
                100,
                2L
        );
        Film newFilm2 = makeFilm(
                "Name 2",
                "Description 2",
                LocalDate.of(2000, 1, 1),
                100,
                2L
        );
        Film savedFilm1 = filmStorage.save(newFilm1);
        Film savedFilm2 = filmStorage.save(newFilm2);

        List<Film> films = filmStorage.findAll();

        assertThat(films).isNotNull();
        assertThat(films).isNotEmpty();
    }

    @Test
    @DisplayName("Должен удалять фильм")
    void shouldDeleteFilm() {
        Film film = makeFilm(
                "To delete",
                "Delete me",
                LocalDate.of(2010, 10, 10),
                90,
                1L
        );
        Film savedFilm = filmStorage.save(film);

        filmStorage.delete(savedFilm.getId());

        Optional<Film> filmOptional = filmStorage.findById(savedFilm.getId());
        assertThat(filmOptional).isEmpty();
    }

    @Test
    @DisplayName("Должен сохранять лайк фильму")
    void shouldSaveLike() {
        User user = makeUser("user1@mail.ru", "user1", "User One", LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.save(user);

        Film film = makeFilm(
                "Liked film",
                "Description",
                LocalDate.of(2015, 5, 5),
                110,
                2L
        );
        Film savedFilm = filmStorage.save(film);

        filmStorage.saveLike(savedUser.getId(), savedFilm.getId());

        List<Film> popular = filmStorage.findPopular(10);

        assertThat(popular)
                .extracting(Film::getId)
                .contains(savedFilm.getId());
    }

    @Test
    @DisplayName("Должен удалять лайк у фильма")
    void shouldDeleteLike() {
        User user = makeUser("user2@mail.ru", "user2", "User Two", LocalDate.of(1992, 2, 2));
        User savedUser = userStorage.save(user);

        Film film = makeFilm(
                "Film with like",
                "Description",
                LocalDate.of(2016, 6, 6),
                115,
                3L
        );
        Film savedFilm = filmStorage.save(film);

        filmStorage.saveLike(savedUser.getId(), savedFilm.getId());
        filmStorage.deleteLike(savedUser.getId(), savedFilm.getId());

        List<Film> popular = filmStorage.findPopular(10);

        assertThat(popular)
                .extracting(Film::getId)
                .doesNotContain(savedFilm.getId());
    }

    @Test
    @DisplayName("Должен возвращать популярные фильмы в порядке лайков")
    void shouldFindPopularFilmsOrderedByLikes() {
        User user1 = userStorage.save(makeUser("a@mail.ru", "aaa", "A", LocalDate.of(1990, 1, 1)));
        User user2 = userStorage.save(makeUser("b@mail.ru", "bbb", "B", LocalDate.of(1991, 1, 1)));
        User user3 = userStorage.save(makeUser("c@mail.ru", "ccc", "C", LocalDate.of(1992, 1, 1)));

        Film film1 = filmStorage.save(makeFilm("Film 1", "Desc 1", LocalDate.of(2020, 1, 1), 100, 1L));
        Film film2 = filmStorage.save(makeFilm("Film 2", "Desc 2", LocalDate.of(2021, 1, 1), 110, 1L));
        Film film3 = filmStorage.save(makeFilm("Film 3", "Desc 3", LocalDate.of(2022, 1, 1), 120, 1L));

        filmStorage.saveLike(user1.getId(), film1.getId());
        filmStorage.saveLike(user2.getId(), film1.getId());

        filmStorage.saveLike(user1.getId(), film2.getId());

        filmStorage.saveLike(user1.getId(), film3.getId());
        filmStorage.saveLike(user2.getId(), film3.getId());
        filmStorage.saveLike(user3.getId(), film3.getId());

        List<Film> popular = filmStorage.findPopular(10);

        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(film3.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(2).getId()).isEqualTo(film2.getId());
    }

    @Test
    @DisplayName("Должен учитывать count в findPopular")
    void shouldReturnLimitedPopularFilms() {
        User user1 = userStorage.save(makeUser("d@mail.ru", "ddd", "D", LocalDate.of(1990, 3, 3)));
        User user2 = userStorage.save(makeUser("e@mail.ru", "eee", "E", LocalDate.of(1991, 3, 3)));

        Film film1 = filmStorage.save(makeFilm("Popular 1", "Desc", LocalDate.of(2020, 3, 3), 100, 1L));
        Film film2 = filmStorage.save(makeFilm("Popular 2", "Desc", LocalDate.of(2021, 3, 3), 100, 1L));
        Film film3 = filmStorage.save(makeFilm("Popular 3", "Desc", LocalDate.of(2022, 3, 3), 100, 1L));

        filmStorage.saveLike(user1.getId(), film1.getId());
        filmStorage.saveLike(user2.getId(), film1.getId());

        filmStorage.saveLike(user1.getId(), film2.getId());

        List<Film> popular = filmStorage.findPopular(2);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film2.getId());
    }

    private Film makeFilm(String name,
                          String description,
                          LocalDate releaseDate,
                          int duration,
                          long mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        return film;
    }

    private User makeUser(String email,
                          String login,
                          String name,
                          LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }
}
