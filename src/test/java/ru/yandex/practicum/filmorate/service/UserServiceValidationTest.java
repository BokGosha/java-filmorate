package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceValidationTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserStorage());
    }

    @Test
    void createUser_withoutName_setsNameToLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday("2001-12-12");

        User created = service.createUser(user);

        assertEquals("testuser", created.getName());
    }

    @Test
    void updateUser_withoutId_throws() {
        User update = new User();
        update.setEmail("new@example.com");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateUser(update));
        assertEquals("Не указан id пользователя", exception.getMessage());
    }

    @Test
    void updateUser_loginWithSpaces_throws() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("valid");
        user.setBirthday("2001-12-12");
        User created = service.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setLogin("invalid login");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateUser(update));
        assertEquals("Логин содержит пробелы", exception.getMessage());
    }

    @Test
    void validateBirthday_futureDate_throws() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("valid");
        user.setBirthday("2027-12-12");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createUser(user));
        assertEquals("День рождения указан в будущем", exception.getMessage());
    }
}
