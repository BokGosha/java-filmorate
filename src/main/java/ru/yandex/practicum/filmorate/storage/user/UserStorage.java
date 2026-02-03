package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> getFriends(long userId);
    Collection<User> getCommonFriends(long user1Id, long user2Id);
    void deleteFriend(long userId, long friendId);
    User saveFriend(long userId, long friendId);
    Collection<User> getUsers();
    User getUser(long userId);
    User saveUser(User user);
    User updateUser(User user);
    void deleteUser(long userId);
}
