package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1);
    }

    @Test
    void testValidName() {
        film.setName("Valid Name");
        assertEquals("Valid Name", film.getName());
    }

    @Test
    void testEmptyNameFails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setName(""));
        assertTrue(exception.getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void testNullNameFails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setName(null));
        assertTrue(exception.getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void testValidDescription() {
        String description = "a".repeat(200);
        film.setDescription(description);
        assertEquals(description, film.getDescription());
    }

    @Test
    void testDescriptionTooLongFails() {
        String description = "a".repeat(201);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setDescription(description));
        assertTrue(exception.getMessage().contains("Описание фильма не может быть длиннее 200 символов"));
    }

    @Test
    void testValidReleaseDate() {
        LocalDate date = LocalDate.of(1895, 12, 28);
        film.setReleaseDate(date);
        assertEquals(date, film.getReleaseDate());
    }

    @Test
    void testReleaseDateBeforeEarliestFails() {
        LocalDate date = LocalDate.of(1895, 12, 27);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setReleaseDate(date));
        assertTrue(exception.getMessage().contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void testNullReleaseDateFails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setReleaseDate(null));
        assertTrue(exception.getMessage().contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void testValidDuration() {
        film.setDuration(90);
        assertEquals(90, film.getDuration());
    }

    @Test
    void testZeroDurationFails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setDuration(0));
        assertTrue(exception.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void testNegativeDurationFails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setDuration(-10));
        assertTrue(exception.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
    }
}
