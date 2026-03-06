//package ru.yandex.practicum.filmorate.storage.user;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.User;
//
//import java.util.*;
//
//@Slf4j
//@Component
//public class InMemoryUserStorage implements UserStorage {
//
//    private final Map<Long, User> users = new HashMap<>();
//
//    @Override
//    public List<User> getFriends(long userId) {
//        containsUser(userId);
//
//        Set<User> friendIds = users.get(userId).getFriends();
//        List<User> friends = new ArrayList<>();
//        for (User friendId : friendIds) {
//            friends.add(users.get(friendId));
//        }
//
//        log.info("Друзья пользователя с id={} получены, количество друзей: {}", userId, friends.size());
//        return friends;
//    }
//
//    @Override
//    public List<User> getCommonFriends(long user1Id, long user2Id) {
//        containsUser(user1Id);
//        containsUser(user2Id);
//
//        Set<User> friends1 = users.get(user1Id).getFriends();
//        Set<User> friends2 = users.get(user2Id).getFriends();
//
//        Set<User> commonFriendIds = new HashSet<>(friends1);
//        commonFriendIds.retainAll(friends2);
//        List<User> friends = new ArrayList<>();
//        for (User friendId : commonFriendIds) {
//            friends.add(users.get(friendId));
//        }
//
//        log.info("Общие друзья пользователей с id={} и id={} получены, количество друзей: {}", user1Id, user2Id,
//                friends.size());
//        return friends;
//    }
//
//    @Override
//    public void deleteFriend(long userId, long friendId) {
//        containsUser(userId);
//        containsUser(friendId);
//
//        users.get(userId).getFriends().remove(friendId);
//        users.get(friendId).getFriends().remove(userId);
//        log.info("Дружба пользователей с id={} и id={} удалена", userId, friendId);
//    }
//
//    @Override
//    public User saveFriend(long userId, long friendId) {
//        containsUser(userId);
//        containsUser(friendId);
//
//        //users.get(userId).getFriends().add(friendId);
//        //users.get(friendId).getFriends().add(userId);
//        log.info("Дружба пользователей с id={} и id={} создана и сохранена", userId, friendId);
//        return users.get(userId);
//    }
//
//    @Override
//    public List<User> getUsers() {
//        return new ArrayList<>(users.values());
//    }
//
//    @Override
//    public User getUser(long userId) {
//        containsUser(userId);
//
//        log.info("Пользователь с id={} получен", userId);
//        return users.get(userId);
//    }
//
//    @Override
//    public User saveUser(User user) {
//        user.setId(getNextId());
//        users.put(user.getId(), user);
//        log.info("Пользователь с id={} создан и сохранён", user.getId());
//        return user;
//    }
//
//    private long getNextId() {
//        long currentId = users.keySet()
//                .stream()
//                .mapToLong(id -> id)
//                .max()
//                .orElse(0);
//        return ++currentId;
//    }
//
//    @Override
//    public User updateUser(User newUser) {
//        containsUser(newUser.getId());
//
//        users.put(newUser.getId(), newUser);
//        log.info("Пользователь с id={} обновлён", newUser.getId());
//        return newUser;
//    }
//
//    @Override
//    public void deleteUser(long userId) {
//        containsUser(userId);
//
//        users.remove(userId);
//        log.info("Пользователь с id={} удалён", userId);
//    }
//
//    private void containsUser(long userId) {
//        if (!users.containsKey(userId)) {
//            log.warn("Пользователь с id={} не найден", userId);
//            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
//        }
//    }
//}
