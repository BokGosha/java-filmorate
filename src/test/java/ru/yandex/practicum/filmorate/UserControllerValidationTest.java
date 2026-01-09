package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerValidationTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    void createUser_withoutName_setsNameToLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday("2001-12-12");

        User created = controller.createUser(user);

        assertEquals("testuser", created.getName());
    }

    @Test
    void updateUser_withoutId_throws() {
        User update = new User();
        update.setEmail("new@example.com");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateUser(update));
        assertEquals("Не указан id пользователя", exception.getMessage());
    }

    @Test
    void updateUser_loginWithSpaces_throws() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("valid");
        user.setBirthday("2001-12-12");
        User created = controller.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setLogin("invalid login");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateUser(update));
        assertEquals("Логин содержит пробелы", exception.getMessage());
    }

    @Test
    void validateBirthday_futureDate_throws() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("valid");
        user.setBirthday("2027-12-12");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createUser(user));
        assertEquals("День рождения указан в будущем", exception.getMessage());
    }
}
