package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public Mpa getMpa(long mpaId) {
        return mpaStorage.findById(mpaId)
                .orElseThrow(() -> {
                    log.warn("Рейтинг с id={} не найден", mpaId);
                    return new NotFoundException("Рейтинг с id=" + mpaId + " не найден");
                });
    }

    public List<Mpa> getAllMpas() {
        return mpaStorage.findAll();
    }
}
