package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private int id;

    @NotBlank(message = "Электронная почта не может быть пустой.")
    @Email(message = "Электронная почта должна содержать символ '@'.")
    private String email;

    @NotBlank(message = "Логин не может быть пустым.")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы.")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;

    public void setName(String name) {
        this.name = (name == null || name.isBlank()) ? login : name;
    }

    @JsonIgnore
    private final Map<Integer, FriendshipStatus> friendStatuses = new HashMap<>();

    public enum FriendshipStatus {
        UNCONFIRMED,  // запрос отправлен, но получатель ещё не подтвердил
        CONFIRMED     // дружба подтверждена (взаимная)
    }
}