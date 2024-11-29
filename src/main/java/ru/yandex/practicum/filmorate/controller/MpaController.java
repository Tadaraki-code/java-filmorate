package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public Collection<MpaRating> getAllMpa() {
        log.info("Запрос на получение списка рейтингов");
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public MpaRating getMpa(@PathVariable("id") Long id) {
        log.info("Запрос на получение рейтинга по ID {}", id);
        return mpaService.getCode(id);
    }
}
