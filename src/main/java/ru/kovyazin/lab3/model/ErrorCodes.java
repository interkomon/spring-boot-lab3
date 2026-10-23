package ru.kovyazin.lab3.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorCodes {
    EMPTY(""),
    VALIDATION_EXCEPTION("ValidationException"),
    UNSUPPORTED_CODE_EXCEPTION("UnsupportedCodeException"),
    UNKNOWN_EXCEPTION("UnknownException");

    private final String value;

    ErrorCodes(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @Override
    public String toString() { return value; }
}
