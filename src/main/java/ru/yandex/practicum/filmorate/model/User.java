package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public void setEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Электронная почта не может быть пустой и должна содержать символ '@'.");
        }
        this.email = email;
    }

    public void setLogin(String login) {
        if (login == null || login.isBlank() || login.contains(" ")) {
            throw new IllegalArgumentException("Логин не может быть пустым и содержать пробелы.");
        }
        this.login = login;
    }

    public void setName(String name) {
        this.name = (name == null || name.isBlank()) ? login : name;
    }

    public void setBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем.");
        }
        this.birthday = birthday;
    }
}