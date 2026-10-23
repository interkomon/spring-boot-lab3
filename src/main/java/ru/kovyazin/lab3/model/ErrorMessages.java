package ru.kovyazin.lab3.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorMessages {
    EMPTY(""),
    VALIDATION("Ошибка валидации"),
    UNSUPPORTED("Неподдерживаемый код uid"),
    UNKNOWN("Произошла непредвиденная ошибка");

    private final String value;

    ErrorMessages(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @Override
    public String toString() { return value; }
}
