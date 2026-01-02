package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MPADbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;
    private final MPADbStorage mpaDbStorage; // Добавлено

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreDbStorage genreDbStorage, MPADbStorage mpaDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDbStorage = genreDbStorage;
        this.mpaDbStorage = mpaDbStorage; // Инициализация
    }

    public Film createFilm(Film film) {
        validateMpa(film);
        film.setGenreDbStorage(genreDbStorage);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        validateMpa(film);
        film.setGenreDbStorage(genreDbStorage);
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        List<Film> films = new ArrayList<>(filmStorage.getAllFilms());
        films.forEach(film -> film.setGenreDbStorage(genreDbStorage));
        return films;
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        film.setGenreDbStorage(genreDbStorage);
        return film;
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        // Сначала в БД
        filmStorage.addLike(filmId, userId);
        // Потом обновляем объект
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        // Сначала в БД
        filmStorage.removeLike(filmId, userId);
        // Потом обновляем объект
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparing(film -> -film.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        return film;
    }

    private void validateMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new IllegalArgumentException("MPA и его id не могут быть null.");
        }
        if (mpaDbStorage.getMPAById(film.getMpa().getId()) == null) {
            throw new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден.");
        }
    }
}
