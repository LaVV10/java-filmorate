package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private LocalValidatorFactoryBean validator;
    private User user;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        user = new User();
        user.setId(1);
        user.setLogin("testuser");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testValidUser() {
        user.setEmail("user@test.com");
        user.setName("User Name");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailIsNull() {
        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Электронная почта не может быть пустой"));
    }

    @Test
    void testEmailIsEmpty() {
        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Электронная почта не может быть пустой"));
    }

    @Test
    void testEmailWithoutAt() {
        user.setEmail("invalid-email");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("символ '@'"));
    }

    @Test
    void testLoginIsNull() {
        user.setLogin(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(hasViolation(violations, "login", "пустым"),
                "Ожидалась ошибка валидации: логин не может быть пустым");
    }

    @Test
    void testLoginIsEmpty() {
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(hasViolation(violations, "login", "пустым"),
                "Ожидалась ошибка валидации: логин не может быть пустым");
    }

    @Test
    void testLoginWithSpaces() {
        user.setLogin("invalid login");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(hasViolation(violations, "login", "пробелы"),
                "Ожидалась ошибка валидации: логин не может содержать пробелы");
    }

    // Вспомогательный метод
    private boolean hasViolation(Set<ConstraintViolation<User>> violations, String field, String messagePart) {
        return violations.stream()
                .anyMatch(v -> field.equals(v.getPropertyPath().toString()) &&
                        v.getMessage().contains(messagePart));
    }

    @Test
    void testBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(hasViolation(violations, "birthday", "будущем"),
                "Ожидалась ошибка валидации: дата рождения не может быть в будущем");
    }
}
