package com.trithai.utils.shortenurl.exceptions;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AliasNotFoundException.class)
    public ResponseEntity<AliasError> handleException(AliasNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(AliasError.aliasNotFound(ex.getAlias()));
    }

    @ExceptionHandler(CreatingAliasExistedException.class)
    public ResponseEntity<AliasError> handleException(CreatingAliasExistedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AliasError.aliasExisted(ex.getAlias()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<AliasError> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        error -> {
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });
        return new ResponseEntity<>(
                AliasError.of(null, "bad_request", "bad_request", errors), HttpStatus.BAD_REQUEST);
    }
}
