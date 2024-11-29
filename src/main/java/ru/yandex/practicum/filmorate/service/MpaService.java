package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.Collection;

@Service
@Getter
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaStorage;

    public Collection<MpaRating> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public MpaRating getCode(Long id) {
        return mpaStorage.getCode(id);
    }
}
