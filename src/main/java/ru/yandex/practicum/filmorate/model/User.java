package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private Long id;

    @Email
    private String email;

    @NotBlank
    private String login;

    private String name;
    private String birthday;

    private Set<Long> friends = new HashSet<>();
}
