package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public class ErrorResponse implements DataModel {

    private final String error;
    private final DataModel errorModel;
    private final Long errorId;

    public ErrorResponse(String error) {
        this.error = error;
        this.errorModel = null;
        this.errorId = 0L;
    }

    public ErrorResponse(DataModel errorModel) {
        this.error = null;
        this.errorModel = errorModel;
        this.errorId = 0L;
    }

    public ErrorResponse(Long errorId) {
        this.error = null;
        this.errorModel = null;
        this.errorId = errorId;
    }

}
