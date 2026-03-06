package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserRequest {

    @NotBlank
    private String login;
    private String name;
    @Email
    private String email;
    private LocalDate birthday;
}
