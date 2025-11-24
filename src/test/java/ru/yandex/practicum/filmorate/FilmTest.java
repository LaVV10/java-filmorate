package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    private LocalValidatorFactoryBean validator;
    private Film film;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        film = new Film();
        film.setId(1);
        film.setName("Default Name");  // ✅ Установим валидное имя по умолчанию
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(90);
    }

    @Test
    void testValidFilm() {
        film.setDescription("Short description");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNameIsNull() {
        film.setName(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void testNameIsEmpty() {
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void testDescriptionTooLong() {
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size()); // ✅ Теперь только одна ошибка — по описанию
        assertTrue(violations.iterator().next().getMessage().contains("длиннее 200 символов"));
    }

    @Test
    void testReleaseDateIsNull() {
        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("не может быть пустой"));
    }

    @Test
    void testReleaseDateBeforeEarliest() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> film.setReleaseDate(LocalDate.of(1895, 12, 27)));
        assertTrue(exception.getMessage().contains("28 декабря 1895 года"));
    }

    @Test
    void testDurationZero() {
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("положительным числом"));
    }

    @Test
    void testDurationNegative() {
        film.setDuration(-10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("положительным числом"));
    }
}
