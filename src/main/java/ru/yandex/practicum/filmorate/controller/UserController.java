package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int generatedId = 1;

    private int generateId() {
        return generatedId++;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user.getLogin());
        validateUser(user);
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id: {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с id: {}", user.getId());
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new IllegalArgumentException("User with id " + user.getId() + " does not exist");
        }
        validateUser(user);
        users.put(user.getId(), user);
        log.info("Пользователь с id {} успешно обновлён", user.getId());
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение списка всех пользователей");
        return new ArrayList<>(users.values());
    }

    private void validateUser(User user) {
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail());
        } else {
            log.error("Валидация провалена: email пустой");
            throw new ValidationException("Email не может быть пустым.");
        }
        if (user.getLogin() != null) {
            user.setLogin(user.getLogin());
        } else {
            log.error("Валидация провалена: логин пустой");
            throw new ValidationException("Login не может быть пустым.");
        }
        user.setName(user.getName());
        if (user.getBirthday() != null) {
            user.setBirthday(user.getBirthday());
        }
    }
}
