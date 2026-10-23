package ru.kovyazin.lab3.service;

import org.springframework.validation.BindingResult;
import ru.kovyazin.lab3.exception.ValidationFailedException;

public interface ValidationService {
    void isValid(BindingResult result) throws ValidationFailedException;
}
