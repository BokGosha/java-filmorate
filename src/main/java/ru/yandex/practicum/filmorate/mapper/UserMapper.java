package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.user.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLogin(user.getLogin());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());

        Set<User> users = user.getFriends();
        if (users != null) {
            Set<FriendDto> friends = user.getFriends()
                    .stream()
                    .map(UserMapper::mapToFriendDto)
                    .collect(Collectors.toSet());
            dto.setFriends(friends);
        }

        return dto;
    }

    public static FriendDto mapToFriendDto(User user) {
        FriendDto dto = new FriendDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLogin(user.getLogin());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());

        return dto;
    }

    public static LikeDto mapToLikeDto(User user) {
        LikeDto dto = new LikeDto();

        dto.setId(user.getId());
        dto.setLogin(user.getLogin());

        return dto;
    }

    public static User mapToUser(NewUserRequest request) {
        User user = new User();

        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setBirthday(request.getBirthday());

        return user;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.hasName()) {
            user.setName(request.getName());
        }

        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }

        if (request.hasEmail()) {
            user.setEmail(request.getEmail());
        }

        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }

        return user;
    }
}
