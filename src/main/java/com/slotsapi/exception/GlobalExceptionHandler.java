package com.slotsapi.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. Обработка занятых слотов и конфликтов бизнес-логики
     * Перехватывает IllegalStateException (например, SLOT_ALREADY_BOOKED)
     * Возвращает статус 409 CONFLICT
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessConflict(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * 2. Обработка неверных входных данных
     * Перехватывает IllegalArgumentException (например, ресурс не найден или неверный формат времени)
     * Возвращает статус 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 3. Обработка нарушений безопасности и прав доступа
     * Перехватывает SecurityException (попытка подменить чужой company_id или ресурс)
     * Возвращает статус 403 FORBIDDEN
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(SecurityException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * 4. Глобальный перехватчик всех остальных непредвиденных ошибок (Fallback)
     * Возвращает статус 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred on the server.");

        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 5. Обработка синтаксических ошибок в JSON и неизвестных полей (Jackson)
     * Перехватывает HttpMessageNotReadableException
     * Возвращает статус 400 BAD REQUEST
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        // Пытаемся достать понятную причину ошибки для b2b-клиента
        String errorMessage = "Malformed JSON request or unrecognized fields.";
        if (ex.getCause() instanceof UnrecognizedPropertyException unpEx) {
            errorMessage = "Unrecognized field: '" + unpEx.getPropertyName() +
                    "'. Known properties are: " + unpEx.getKnownPropertyIds();
        } else if (ex.getMessage() != null) {
            errorMessage = ex.getMessage();
        }

        body.put("message", errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 6. Обработка ошибок валидации JSR-380 (@NotNull, @NotEmpty и т.д.)
     * Перехватывает MethodArgumentNotValidException
     * Возвращает статус 400 BAD REQUEST
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        // Собираем все ошибки валидации полей в одну понятную строку
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorMessage.append("[").append(error.getField()).append(" : ").append(error.getDefaultMessage()).append("] ")
        );

        body.put("message", errorMessage.toString().trim());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<String> handleOAuth(OAuthException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
