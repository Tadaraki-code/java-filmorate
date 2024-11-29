package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import ru.yandex.practicum.filmorate.model.DataModel;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

@Getter
public class NotFoundException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public NotFoundException(String message, DataModel model) {
        super(message);
        this.errorResponse = new ErrorResponse(model);
    }

    public NotFoundException(String message, Long id) {
        super(message);
        this.errorResponse = new ErrorResponse(id);
    }

    public NotFoundException(DataModel model) {
        super();
        this.errorResponse = new ErrorResponse(model);
    }

    public NotFoundException(String message) {
        super(message);
        errorResponse = null;
    }

    public NotFoundException() {
        super();
        errorResponse = null;
    }

}
