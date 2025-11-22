package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int generatedId = 1;

    private int generateId() {
        return generatedId++;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());
        validateFilm(film);
        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с id: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id: {}", film.getId());
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new IllegalArgumentException("Film with id " + film.getId() + " does not exist");
        }
        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} успешно обновлён", film.getId());
        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return new ArrayList<>(films.values());
    }

    private void validateFilm(Film film) {
        if (film.getName() != null) {
            film.setName(film.getName());
        } else {
            log.error("Валидация провалена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription() != null) {
            film.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            film.setReleaseDate(film.getReleaseDate());
        } else {
            log.error("Валидация провалена: дата релиза пустая");
            throw new ValidationException("Дата релиза не может быть пустой.");
        }
        film.setDuration(film.getDuration());
    }
}
