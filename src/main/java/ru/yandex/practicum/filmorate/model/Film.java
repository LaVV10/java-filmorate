package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым.");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("Описание фильма не может быть длиннее 200 символов.");
        }
        this.description = description;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (releaseDate == null || releaseDate.isBefore(earliestDate)) {
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        this.releaseDate = releaseDate;
    }

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Продолжительность фильма должна быть положительным числом.");
        }
        this.duration = duration;
    }
}
