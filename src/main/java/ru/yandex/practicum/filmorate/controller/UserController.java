package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.FriendDto;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable long id) {
        return userService.getUser(id);
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping
    public UserDto updateUser(@RequestBody UpdateUserRequest request) {
        return userService.updateUser(request.getId(), request);
    }

    @GetMapping("/{id}/friends")
    public List<FriendDto> getFriends(@PathVariable Long id) {
       return userService.getFriends(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public UserDto addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<FriendDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
