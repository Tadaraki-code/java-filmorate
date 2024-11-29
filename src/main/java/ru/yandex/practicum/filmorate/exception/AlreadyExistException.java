package ru.yandex.practicum.filmorate.exception;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String message) {
        super(message);
    }

    public AlreadyExistException(String message, Exception e) {
        super(message, e);
    }
}
