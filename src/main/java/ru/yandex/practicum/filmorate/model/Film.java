package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    @Size(max = 200, message = "Описание фильма не может быть длиннее 200 символов.")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой.")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;

    @JsonIgnore
    @Builder.Default
    private Set<Integer> genreIds = new HashSet<>();

    @JsonIgnore
    @Builder.Default
    private final Set<Integer> likes = new HashSet<>();

    private MPA mpa;

    @JsonProperty("genres")
    public List<Genre> getGenres() {
        List<Genre> genres = new ArrayList<>();
        if (genreIds != null) {
            for (Integer id : genreIds) {
                Genre genre = new Genre();
                genre.setId(id);
                // Проверяем, что genreDbStorage инициализирован
                if (genreDbStorage != null) {
                    Genre fullGenre = genreDbStorage.getGenreById(id);
                    if (fullGenre != null) {
                        genre.setName(fullGenre.getName());
                    }
                }
                genres.add(genre);
            }
        }
        return genres;
    }

    @JsonProperty("genres")
    public void setGenres(List<Genre> genres) {
        if (genres != null) {
            genreIds.clear();
            genres.forEach(genre -> genreIds.add(genre.getId()));
        }
    }

    // Добавляем зависимость для доступа к жанрам
    @JsonIgnore
    @Setter
    private transient GenreDbStorage genreDbStorage;

    public void setReleaseDate(LocalDate releaseDate) {
        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (releaseDate != null && releaseDate.isBefore(earliestDate)) {
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        this.releaseDate = releaseDate;
    }
}