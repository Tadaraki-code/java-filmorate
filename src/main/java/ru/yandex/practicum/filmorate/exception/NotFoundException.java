package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import ru.yandex.practicum.filmorate.model.DataModel;

@Getter
public class NotFoundException extends RuntimeException {
    private final DataModel model;

    public NotFoundException(String message, DataModel model) {
        super(message);
        this.model = model;
    }

    public NotFoundException(DataModel model) {
        super();
        this.model = model;
    }

    public NotFoundException(String message) {
        super(message);
        model = null;
    }

    public NotFoundException() {
        super();
        model = null;
    }

}
