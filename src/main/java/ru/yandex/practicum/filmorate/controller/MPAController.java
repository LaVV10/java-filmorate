package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.MPADbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MPAController {

    private final MPADbStorage mpaDbStorage;

    @Autowired
    public MPAController(MPADbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    @GetMapping
    public List<MPA> getAllMPA() {
        return mpaDbStorage.getAllMPA();
    }

    @GetMapping("/{id}")
    public MPA getMPAById(@PathVariable int id) {
        MPA mpa = mpaDbStorage.getMPAById(id);
        if (mpa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Рейтинг с id " + id + " не найден.");
        }
        return mpa;
    }
}
