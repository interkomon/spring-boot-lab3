package ru.kovyazin.lab3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ru.kovyazin.lab3.exception.ValidationFailedException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestValidationService implements ValidationService {
    @Override
    public void isValid(BindingResult result) throws ValidationFailedException {
        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error ->
                    log.error("BindingResult: {}: {}", error.getField(),
                            error.getDefaultMessage()));
            String details = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .sorted().collect(Collectors.joining("; "));
            log.error("Выбрасывается ValidationFailedException: {}", details);
            throw new ValidationFailedException(details);
        }
        log.info("Валидация Request завершена успешно");
    }
}
