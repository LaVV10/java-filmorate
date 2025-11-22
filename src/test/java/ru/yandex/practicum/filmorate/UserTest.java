package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setLogin("testuser");
    }

    @Test
    void testValidEmail() {
        user.setEmail("user@test.com");
        assertEquals("user@test.com", user.getEmail());
    }

    @Test
    void testEmptyEmail() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> user.setEmail(""));
        assertTrue(exception.getMessage().contains("Электронная почта не может быть пустой"));
    }

    @Test
    void testNullEmail() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> user.setEmail(null));
        assertTrue(exception.getMessage().contains("Электронная почта не может быть пустой"));
    }

    @Test
    void testEmailWithoutAt() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> user.setEmail("invalid-email"));
        assertTrue(exception.getMessage().contains("Электронная почта не может быть пустой и должна содержать символ '@'"));
    }

    @Test
    void testValidLogin() {
        user.setLogin("validlogin");
        assertEquals("validlogin", user.getLogin());
    }

    @Test
    void testEmptyLogin() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> user.setLogin(""));
        assertTrue(exception.getMessage().contains("Логин не может быть пустым"));
    }

    @Test
    void testLoginWithSpaces() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> user.setLogin("invalid login"));
        assertTrue(exception.getMessage().contains("Логин не может быть пустым и содержать пробелы"));
    }

    @Test
    void testNameIsNullUsesLogin() {
        user.setName(null);
        assertEquals("testuser", user.getName());
    }

    @Test
    void testNameIsBlankUsesLogin() {
        user.setName("   ");
        assertEquals("testuser", user.getName());
    }

    @Test
    void testValidBirthday() {
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthday());
    }

    @Test
    void testFutureBirthdayFails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> user.setBirthday(LocalDate.now().plusDays(1)));
        assertTrue(exception.getMessage().contains("Дата рождения не может быть в будущем"));
    }
}
