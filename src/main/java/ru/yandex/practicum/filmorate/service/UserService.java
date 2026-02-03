package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User getUser(long id) {
        return userStorage.getUser(id);
    }

    public User addFriend(long userId, long friendId) {
        return userStorage.saveFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(long userId, long friendId) {
        return userStorage.getCommonFriends(userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        return userStorage.getFriends(userId);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        validateBirthday(user.getBirthday());

        if (user.getName() == null) {
            user.setName(user.getLogin());
            log.debug("У пользователя не было имени, установлено name = login: {}", user.getName());
        }

        return userStorage.saveUser(user);
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Не указан id пользователя");
        }

        if (newUser.getLogin() != null) {
            validateLogin(newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            validateBirthday(newUser.getBirthday());
        }

        return userStorage.updateUser(newUser);
    }

    public void deleteUser(long userId) {
        userStorage.deleteUser(userId);
    }

    private void validateLogin(String login) {
        if (login.contains(" ")) {
            throw new ValidationException("Логин содержит пробелы");
        }
    }

    private void validateBirthday(String birthday) {
        if (LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd")).isAfter(LocalDate.now())) {
            throw new ValidationException("День рождения указан в будущем");
        }
    }
}
