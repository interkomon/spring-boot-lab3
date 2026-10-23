package ru.kovyazin.lab3.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kovyazin.lab3.model.*;
import ru.kovyazin.lab3.util.DateTimeUtil;
import java.util.Date;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> unreadable(HttpMessageNotReadableException e) {
        log.error("Ошибка чтения JSON: {}", e.getMostSpecificCause().getMessage());
        Response response = Response.builder().uid("").operationUid("")
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.FAILED).errorCode(ErrorCodes.VALIDATION_EXCEPTION)
                .errorMessage(ErrorMessages.VALIDATION).build();
        log.info("Создан и отправлен response HTTP 400: {}", response);
        return ResponseEntity.badRequest().body(response);
    }
}
