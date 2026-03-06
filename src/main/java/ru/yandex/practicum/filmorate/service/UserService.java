package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.user.FriendDto;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<UserDto> getUsers() {
        return userStorage.findAll()
                .stream()
                .peek(this::setFriends)
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto getUser(long userId) {
        User user = getUserById(userId);

        setFriends(user);

        return UserMapper.mapToUserDto(user);
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
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        userStorage.saveFriend(userId, friendId);

        setFriends(user);

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

        return userStorage.findCommonFriends(user1Id, user2Id)
                .stream()
                .peek(this::setFriends)
                .map(UserMapper::mapToFriendDto)
                .toList();
    }

    public List<FriendDto> getFriends(long userId) {
        User user = getUserById(userId);

        return userStorage.findAllFriends(userId)
                .stream()
                .peek(this::setFriends)
                .map(UserMapper::mapToFriendDto)
                .toList();
    }

    private void setFriends(User friend) {
        Set<User> friends = userStorage.findAllFriends(friend.getId());
        friend.setFriends(friends);
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
