package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("GET /users: возвращается {} пользователей", users.size());

        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("POST /users: получен запрос создания пользователя: email='{}', login='{}', name='{}', birthday='{}'",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        validateBirthday(user.getBirthday());

        if (user.getName() == null) {
            user.setName(user.getLogin());

            log.debug("У пользователя не было имени, установлено name = login: {}", user.getName());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Пользователь создан с id={} и сохранён", user.getId());

        return user;
    }

    private long getNextId() {
        long currentId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentId;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("PUT /users: запрос обновления пользователя id={}", newUser.getId());

        if (newUser.getId() == null) {
            throw new ValidationException("Не указан id пользователя");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.debug("Найден пользователь для обновления: {}", oldUser);

            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }

            if (newUser.getLogin() != null) {
                validateLogin(newUser.getLogin());

                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getBirthday() != null) {
                validateBirthday(newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }

            log.info("Пользователь id={} обновлён", newUser.getId());

            return oldUser;
        }

        log.warn("Пользователь с id={} не найден", newUser.getId());

        throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
    }

    public void validateLogin(String login) {
        if (login.contains(" ")) {
            throw new ValidationException("Логин содержит пробелы");
        }
    }

    public void validateBirthday(String birthday) {
        if (LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd")).isAfter(LocalDate.now())) {
            throw new ValidationException("День рождения указан в будущем");
        }
    }
}
