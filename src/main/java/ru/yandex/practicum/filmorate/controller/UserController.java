package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int generatedId = 1;

    private int generateId() {
        return generatedId++;
    }

    @PostMapping
    public User createUser(@RequestBody @Valid User user) {
        log.info("Получен запрос на создание пользователя: {}", user.getLogin());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id: {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        log.info("Получен запрос на обновление пользователя с id: {}", user.getId());
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new IllegalArgumentException("User with id " + user.getId() + " does not exist");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с id {} успешно обновлён", user.getId());
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение списка всех пользователей");
        return new ArrayList<>(users.values());
    }
}
