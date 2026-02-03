package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserStorageTest {

    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday("2000-01-01");
        return user;
    }

    @Test
    void saveUser_shouldAssignIdAndStore() {
        User user = createUser("u1@mail.ru", "user1", "User One");

        User saved = userStorage.saveUser(user);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals(saved, userStorage.getUser(saved.getId()));
    }

    @Test
    void getUser_notExisting_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getUser(999L));
    }

    @Test
    void updateUser_existing_shouldUpdate() {
        User user = createUser("u1@mail.ru", "user1", "User One");
        User saved = userStorage.saveUser(user);

        User updated = createUser("new@mail.ru", "newLogin", "New Name");
        updated.setId(saved.getId());

        User result = userStorage.updateUser(updated);

        assertEquals("newLogin", userStorage.getUser(saved.getId()).getLogin());
        assertEquals("newLogin", result.getLogin());
        assertEquals("new@mail.ru", result.getEmail());
    }

    @Test
    void updateUser_notExisting_shouldThrowNotFound() {
        User user = createUser("u1@mail.ru", "user1", "User One");
        user.setId(100L);

        assertThrows(NotFoundException.class, () -> userStorage.updateUser(user));
    }

    @Test
    void deleteUser_existing_shouldRemove() {
        User user = createUser("u1@mail.ru", "user1", "User One");
        User saved = userStorage.saveUser(user);

        userStorage.deleteUser(saved.getId());

        assertThrows(NotFoundException.class, () -> userStorage.getUser(saved.getId()));
    }

    @Test
    void deleteUser_notExisting_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.deleteUser(999L));
    }

    @Test
    void saveFriend_shouldCreateMutualFriendship() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));
        User u2 = userStorage.saveUser(createUser("u2@mail.ru", "user2", "User Two"));

        User result = userStorage.saveFriend(u1.getId(), u2.getId());

        Set<Long> friends1 = userStorage.getUser(u1.getId()).getFriends();
        Set<Long> friends2 = userStorage.getUser(u2.getId()).getFriends();

        assertTrue(friends1.contains(u2.getId()));
        assertTrue(friends2.contains(u1.getId()));
        assertEquals(u1.getId(), result.getId());
    }

    @Test
    void saveFriend_notExistingUser_shouldThrowNotFound() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));

        assertThrows(NotFoundException.class, () -> userStorage.saveFriend(u1.getId(), 999L));
        assertThrows(NotFoundException.class, () -> userStorage.saveFriend(999L, u1.getId()));
    }

    @Test
    void deleteFriend_shouldRemoveMutualFriendship() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));
        User u2 = userStorage.saveUser(createUser("u2@mail.ru", "user2", "User Two"));

        userStorage.saveFriend(u1.getId(), u2.getId());
        userStorage.deleteFriend(u1.getId(), u2.getId());

        assertFalse(userStorage.getUser(u1.getId()).getFriends().contains(u2.getId()));
        assertFalse(userStorage.getUser(u2.getId()).getFriends().contains(u1.getId()));
    }

    @Test
    void deleteFriend_notExistingUser_shouldThrowNotFound() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));

        assertThrows(NotFoundException.class, () -> userStorage.deleteFriend(u1.getId(), 999L));
        assertThrows(NotFoundException.class, () -> userStorage.deleteFriend(999L, u1.getId()));
    }

    @Test
    void getFriends_shouldReturnAllFriends() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));
        User u2 = userStorage.saveUser(createUser("u2@mail.ru", "user2", "User Two"));
        User u3 = userStorage.saveUser(createUser("u3@mail.ru", "user3", "User Three"));

        userStorage.saveFriend(u1.getId(), u2.getId());
        userStorage.saveFriend(u1.getId(), u3.getId());

        Collection<User> friends = userStorage.getFriends(u1.getId());

        assertEquals(2, friends.size());
        List<Long> friendIds = friends.stream().map(User::getId).toList();
        assertTrue(friendIds.contains(u2.getId()));
        assertTrue(friendIds.contains(u3.getId()));
    }

    @Test
    void getFriends_notExistingUser_shouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getFriends(999L));
    }

    @Test
    void getCommonFriends_shouldReturnIntersection() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));
        User u2 = userStorage.saveUser(createUser("u2@mail.ru", "user2", "User Two"));
        User u3 = userStorage.saveUser(createUser("u3@mail.ru", "user3", "User Three"));
        User u4 = userStorage.saveUser(createUser("u4@mail.ru", "user4", "User Four"));

        userStorage.saveFriend(u1.getId(), u3.getId());
        userStorage.saveFriend(u1.getId(), u4.getId());
        userStorage.saveFriend(u2.getId(), u3.getId());

        Collection<User> common = userStorage.getCommonFriends(u1.getId(), u2.getId());

        assertEquals(1, common.size());
        assertEquals(u3.getId(), common.iterator().next().getId());
    }

    @Test
    void getCommonFriends_noCommonShouldReturnEmpty() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));
        User u2 = userStorage.saveUser(createUser("u2@mail.ru", "user2", "User Two"));
        User u3 = userStorage.saveUser(createUser("u3@mail.ru", "user3", "User Three"));

        userStorage.saveFriend(u1.getId(), u3.getId());

        Collection<User> common = userStorage.getCommonFriends(u1.getId(), u2.getId());

        assertTrue(common.isEmpty());
    }

    @Test
    void getCommonFriends_notExistingUser_shouldThrowNotFound() {
        User u1 = userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));

        assertThrows(NotFoundException.class, () -> userStorage.getCommonFriends(u1.getId(), 999L));
        assertThrows(NotFoundException.class, () -> userStorage.getCommonFriends(999L, u1.getId()));
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        assertTrue(userStorage.getUsers().isEmpty());

        userStorage.saveUser(createUser("u1@mail.ru", "user1", "User One"));
        userStorage.saveUser(createUser("u2@mail.ru", "user2", "User Two"));

        assertEquals(2, userStorage.getUsers().size());
    }
}
