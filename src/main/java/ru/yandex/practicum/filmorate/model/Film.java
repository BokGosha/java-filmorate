package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private Long id;

    @NotBlank
    private String name;

    private String description;
    private String releaseDate;
    private Integer duration;

    private Set<Long> likes = new HashSet<>();
}
