package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dal.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        DbUserStorage.class,
        DbFilmStorage.class,
        UserRowMapper.class,
        FilmRowMapper.class
})
class UserStorageTest {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmStorage filmStorage;

    @Test
    @DisplayName("Должен находить пользователя по id")
    void shouldFindUserById() {
        User savedUser = userStorage.save(makeUser(
                "findbyid@mail.ru",
                "findbyid",
                "Find By Id",
                LocalDate.of(1995, 5, 5)
        ));

        Optional<User> userOptional = userStorage.findById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(savedUser.getId());
                    assertThat(user.getEmail()).isEqualTo("findbyid@mail.ru");
                    assertThat(user.getLogin()).isEqualTo("findbyid");
                });
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional, если пользователь не найден")
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> userOptional = userStorage.findById(9999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    @DisplayName("Должен сохранять пользователя")
    void shouldSaveUser() {
        User user = makeUser(
                "save@mail.ru",
                "saveUser",
                "Save User",
                LocalDate.of(1990, 1, 1)
        );

        User savedUser = userStorage.save(user);

        assertThat(savedUser.getId()).isPositive();

        Optional<User> userOptional = userStorage.findById(savedUser.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getEmail()).isEqualTo("save@mail.ru");
                    assertThat(found.getLogin()).isEqualTo("saveUser");
                    assertThat(found.getName()).isEqualTo("Save User");
                    assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    @DisplayName("Должен обновлять пользователя")
    void shouldUpdateUser() {
        User savedUser = userStorage.save(makeUser(
                "old@mail.ru",
                "oldLogin",
                "Old Name",
                LocalDate.of(1991, 1, 1)
        ));

        savedUser.setEmail("new@mail.ru");
        savedUser.setLogin("newLogin");
        savedUser.setName("New Name");
        savedUser.setBirthday(LocalDate.of(1992, 2, 2));

        User updatedUser = userStorage.update(savedUser);

        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());

        Optional<User> userOptional = userStorage.findById(savedUser.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getEmail()).isEqualTo("new@mail.ru");
                    assertThat(found.getLogin()).isEqualTo("newLogin");
                    assertThat(found.getName()).isEqualTo("New Name");
                    assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1992, 2, 2));
                });
    }

    @Test
    @DisplayName("Должен возвращать всех пользователей")
    void shouldFindAllUsers() {
        userStorage.save(makeUser("all1@mail.ru", "all1", "All One", LocalDate.of(1990, 1, 1)));
        userStorage.save(makeUser("all2@mail.ru", "all2", "All Two", LocalDate.of(1991, 1, 1)));

        List<User> users = userStorage.findAll();

        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        assertThat(users)
                .extracting(User::getEmail)
                .contains("all1@mail.ru", "all2@mail.ru");
    }

    @Test
    @DisplayName("Должен удалять пользователя")
    void shouldDeleteUser() {
        User savedUser = userStorage.save(makeUser(
                "delete@mail.ru",
                "deleteUser",
                "Delete User",
                LocalDate.of(1993, 3, 3)
        ));

        boolean deleted = userStorage.delete(savedUser.getId());

        assertThat(deleted).isTrue();
        assertThat(userStorage.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Должен добавлять пользователя в друзья")
    void shouldSaveFriend() {
        User user1 = userStorage.save(makeUser("f1@mail.ru", "f1", "Friend One", LocalDate.of(1990, 1, 1)));
        User user2 = userStorage.save(makeUser("f2@mail.ru", "f2", "Friend Two", LocalDate.of(1991, 1, 1)));

        userStorage.saveFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.findAllFriends(user1.getId());

        assertThat(friends)
                .extracting(User::getId)
                .contains(user2.getId());
    }

    @Test
    @DisplayName("Должен удалять пользователя из друзей")
    void shouldDeleteFriend() {
        User user1 = userStorage.save(makeUser("df1@mail.ru", "df1", "Delete Friend One", LocalDate.of(1990, 1, 1)));
        User user2 = userStorage.save(makeUser("df2@mail.ru", "df2", "Delete Friend Two", LocalDate.of(1991, 1, 1)));

        userStorage.saveFriend(user1.getId(), user2.getId());
        userStorage.deleteFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.findAllFriends(user1.getId());

        assertThat(friends)
                .extracting(User::getId)
                .doesNotContain(user2.getId());
    }

    @Test
    @DisplayName("Должен возвращать всех друзей пользователя")
    void shouldFindAllFriends() {
        User user = userStorage.save(makeUser("main@mail.ru", "mainUser", "Main User", LocalDate.of(1990, 1, 1)));
        User friend1 = userStorage.save(makeUser("fr1@mail.ru", "fr1", "Friend 1", LocalDate.of(1991, 1, 1)));
        User friend2 = userStorage.save(makeUser("fr2@mail.ru", "fr2", "Friend 2", LocalDate.of(1992, 2, 2)));

        userStorage.saveFriend(user.getId(), friend1.getId());
        userStorage.saveFriend(user.getId(), friend2.getId());

        Set<User> friends = userStorage.findAllFriends(user.getId());

        assertThat(friends).hasSize(2);
        assertThat(friends)
                .extracting(User::getId)
                .contains(friend1.getId(), friend2.getId());
    }

    @Test
    @DisplayName("Должен возвращать общих друзей пользователей")
    void shouldFindCommonFriends() {
        User user1 = userStorage.save(makeUser("cf1@mail.ru", "cf1", "Common 1", LocalDate.of(1990, 1, 1)));
        User user2 = userStorage.save(makeUser("cf2@mail.ru", "cf2", "Common 2", LocalDate.of(1991, 1, 1)));
        User commonFriend = userStorage.save(makeUser("common@mail.ru", "common", "Common Friend", LocalDate.of(1992, 2, 2)));
        User notCommonFriend = userStorage.save(makeUser("notcommon@mail.ru", "notcommon", "Not Common", LocalDate.of(1993, 3, 3)));

        userStorage.saveFriend(user1.getId(), commonFriend.getId());
        userStorage.saveFriend(user2.getId(), commonFriend.getId());
        userStorage.saveFriend(user1.getId(), notCommonFriend.getId());

        List<User> commonFriends = new ArrayList<>(userStorage.findCommonFriends(user1.getId(), user2.getId()));

        assertThat(commonFriends)
                .extracting(User::getId)
                .contains(commonFriend.getId())
                .doesNotContain(notCommonFriend.getId());
    }

    @Test
    @DisplayName("Должен возвращать пустой список общих друзей, если их нет")
    void shouldReturnEmptyCommonFriendsList() {
        User user1 = userStorage.save(makeUser("ecf1@mail.ru", "ecf1", "ECF1", LocalDate.of(1990, 1, 1)));
        User user2 = userStorage.save(makeUser("ecf2@mail.ru", "ecf2", "ECF2", LocalDate.of(1991, 1, 1)));
        User friend1 = userStorage.save(makeUser("only1@mail.ru", "only1", "Only 1", LocalDate.of(1992, 1, 1)));
        User friend2 = userStorage.save(makeUser("only2@mail.ru", "only2", "Only 2", LocalDate.of(1993, 1, 1)));

        userStorage.saveFriend(user1.getId(), friend1.getId());
        userStorage.saveFriend(user2.getId(), friend2.getId());

        List<User> commonFriends = new ArrayList<>(userStorage.findCommonFriends(user1.getId(), user2.getId()));

        assertThat(commonFriends).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать пользователей, поставивших лайк фильму")
    void shouldFindAllLikes() {
        User user1 = userStorage.save(makeUser("like1@mail.ru", "like1", "Like 1", LocalDate.of(1990, 1, 1)));
        User user2 = userStorage.save(makeUser("like2@mail.ru", "like2", "Like 2", LocalDate.of(1991, 1, 1)));

        Film film = filmStorage.save(makeFilm(
                "Liked film",
                "Description",
                LocalDate.of(2020, 1, 1),
                100,
                1L
        ));

        filmStorage.saveLike(user1.getId(), film.getId());
        filmStorage.saveLike(user2.getId(), film.getId());

        List<User> likes = userStorage.findAllLikes(film.getId());

        assertThat(likes).hasSize(2);
        assertThat(likes)
                .extracting(User::getId)
                .contains(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("Должен возвращать пустой список лайков, если у фильма нет лайков")
    void shouldReturnEmptyLikesList() {
        Film film = filmStorage.save(makeFilm(
                "No likes film",
                "Description",
                LocalDate.of(2021, 1, 1),
                90,
                1L
        ));

        List<User> likes = userStorage.findAllLikes(film.getId());

        assertThat(likes).isEmpty();
    }

    private User makeUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
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
}
