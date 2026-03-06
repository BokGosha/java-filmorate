package ru.yandex.practicum.filmorate.dto.film;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;

@Data
public class UpdateFilmRequest {

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Long id;
    private Mpa mpa;

    public boolean hasName() {
        return !(name == null || name.isEmpty());
    }

    public boolean hasDescription() {
        return !(description == null || description.isEmpty());
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null && duration > 0;
    }

    public boolean hasRating() {
        return mpa != null;
    }
}
