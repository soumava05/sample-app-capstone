package com.capstone.todo.web.api;

import com.capstone.todo.exception.StorageException;
import com.capstone.todo.exception.TaskNotFoundException;
import com.capstone.todo.exception.UserNotFoundException;
import com.capstone.todo.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<Object> details = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("field", fieldError.getField());
            detail.put("message", fieldError.getDefaultMessage());
            details.add(detail);
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.of("VALIDATION_ERROR", "Validation failed", details, correlationId()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(ValidationException exception) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.of("VALIDATION_ERROR", exception.getMessage(), correlationId()));
    }

    @ExceptionHandler({TaskNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception) {
        String code = exception instanceof TaskNotFoundException ? "TASK_NOT_FOUND" : "USER_NOT_FOUND";
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse.of(code, exception.getMessage(), correlationId()));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageException(StorageException exception) {
        log.error("Storage error", exception);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse.of("STORAGE_ERROR", "Storage failure", correlationId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception) {
        log.error("Unhandled error", exception);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", correlationId()));
    }

    private String correlationId() {
        return MDC.get("correlationId");
    }
}
