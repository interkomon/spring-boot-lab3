package ru.kovyazin.lab3.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.kovyazin.lab3.exception.UnsupportedCodeException;
import ru.kovyazin.lab3.exception.ValidationFailedException;
import ru.kovyazin.lab3.model.*;
import ru.kovyazin.lab3.service.ModifyResponseService;
import ru.kovyazin.lab3.service.ValidationService;
import ru.kovyazin.lab3.util.DateTimeUtil;
import java.util.Date;

@Slf4j
@RestController
public class MyController {
    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;

    public MyController(ValidationService validationService,
            @Qualifier("ModifySystemTimeResponseService")
            ModifyResponseService modifyResponseService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
    }

    @PostMapping(value = "/feedback", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Response> feedback(
            @Valid @RequestBody Request request, BindingResult result) {
        log.info("Получен request: {}", request);
        Response response = Response.builder()
                .uid(request.getUid() == null ? "" : request.getUid())
                .operationUid(request.getOperationUid() == null
                        ? "" : request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS).errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY).build();
        log.info("Создан response: {}", response);
        try {
            validationService.isValid(result);
            if ("123".equals(request.getUid())) {
                log.error("uid={} выбрасывается UnsupportedCodeException", request.getUid());
                throw new UnsupportedCodeException("Неподдерживаемый код uid: 123");
            }
            response = modifyResponseService.modify(response);
            log.info("Отправлен response HTTP 200: {}", response);
            return ResponseEntity.ok(response);
        } catch (ValidationFailedException e) {
            return error(response, ErrorCodes.VALIDATION_EXCEPTION,
                    ErrorMessages.VALIDATION, 400);
        } catch (UnsupportedCodeException e) {
            return error(response, ErrorCodes.UNSUPPORTED_CODE_EXCEPTION,
                    ErrorMessages.UNSUPPORTED, 400);
        } catch (Exception e) {
            log.error("uid={} непредвиденная ошибка", request.getUid(), e);
            return error(response, ErrorCodes.UNKNOWN_EXCEPTION,
                    ErrorMessages.UNKNOWN, 500);
        }
    }

    private ResponseEntity<Response> error(Response response, ErrorCodes code,
            ErrorMessages message, int status) {
        response.setCode(Codes.FAILED);
        response.setErrorCode(code);
        response.setErrorMessage(message);
        log.info("Изменён и отправлен response HTTP {}: {}", status, response);
        return ResponseEntity.status(status).body(response);
    }
}
