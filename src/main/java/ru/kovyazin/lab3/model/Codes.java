package ru.kovyazin.lab3.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Codes {
    SUCCESS("success"),
    FAILED("failed");

    private final String value;

    Codes(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @Override
    public String toString() { return value; }
}
