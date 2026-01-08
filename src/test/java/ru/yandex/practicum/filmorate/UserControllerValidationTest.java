package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerValidationTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user1",
            "user_login123",
            "a"
    })
    void validateLogin_withoutSpaces_ok(String validLogin) {
        assertDoesNotThrow(() -> controller.validateLogin(validLogin));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "login with space",
            " login",
            "login ",
            " l o g i n "
    })
    void validateLogin_withSpaces_throws(String invalidLogin) {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.validateLogin(invalidLogin));
        assertEquals("Логин содержит пробелы", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "2000-01-01, true",
            "2023-12-31, true",
            "2026-01-06, true",
            "2026-01-07, false"
    })
    void validateBirthday(String birthdayStr, boolean expectedValid) {
        if (expectedValid) {
            assertDoesNotThrow(() -> controller.validateBirthday(birthdayStr));
        } else {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> controller.validateBirthday(birthdayStr));
            assertEquals("День рождения указан в будущем", exception.getMessage());
        }
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
}
