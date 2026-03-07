package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.user.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<UserDto> getUsers() {
        List<User> users = userStorage.findAll();
        setFriendsForUsers(users);

        return users.stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto getUser(long userId) {
        User user = getUserById(userId);

        Set<User> friends = userStorage.findAllFriends(userId);
        user.setFriends(friends);

        return UserMapper.mapToUserDto(user);
    }

    public Map<Long, Set<LikeDto>> getAllLikesByFilmIds(List<Long> filmIds) {
        return userStorage.findLikesForFilmsIds(filmIds);
    }

    public UserDto createUser(NewUserRequest request) {
        validateLogin(request.getLogin());
        validateBirthday(request.getBirthday());

        if (request.getName() == null) {
            request.setName(request.getLogin());
            log.debug("У пользователя не было имени, установлено name = login: {}", request.getName());
        }

        User user = UserMapper.mapToUser(request);

        user = userStorage.save(user);

        return UserMapper.mapToUserDto(user);
    }

    public Set<User> getAllLikesByFilmId(long filmId) {
        return new HashSet<>(userStorage.findAllLikes(filmId));
    }

    public UserDto updateUser(long userId, UpdateUserRequest request) {
        if (request.getLogin() != null) {
            validateLogin(request.getLogin());
        }

        if (request.getBirthday() != null) {
            validateBirthday(request.getBirthday());
        }

        User updatedUser = UserMapper.updateUserFields(getUserById(userId), request);

        updatedUser = userStorage.update(updatedUser);

        return UserMapper.mapToUserDto(updatedUser);
    }

    public UserDto addFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.saveFriend(userId, friendId);

        User user = getUserById(userId);

        return UserMapper.mapToUserDto(user);
    }

    public void deleteFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.deleteFriend(userId, friendId);
    }

    public List<FriendDto> getCommonFriends(long user1Id, long user2Id) {
        getUserById(user1Id);
        getUserById(user2Id);

        List<User> commonFriends = new ArrayList<>(userStorage.findCommonFriends(user1Id, user2Id));
        setFriendsForUsers(commonFriends);

        return commonFriends
                .stream()
                .map(UserMapper::mapToFriendDto)
                .toList();
    }

    public List<FriendDto> getFriends(long userId) {
        getUserById(userId);

        List<User> friends = new ArrayList<>(userStorage.findAllFriends(userId));
        setFriendsForUsers(friends);

        return friends.stream()
                .map(UserMapper::mapToFriendDto)
                .toList();
    }

    private void setFriendsForUsers(List<User> users) {
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        Map<Long, Set<User>> friendsByUserIds =
                userStorage.findFriendsForUserIds(userIds);

        users.forEach(u ->
                u.setFriends(friendsByUserIds.get(u.getId()))
        );
    }

    private User getUserById(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });
    }

    private void validateLogin(String login) {
        if (login.contains(" ")) {
            throw new ValidationException("Логин содержит пробелы");
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("День рождения указан в будущем");
        }
    }
}
