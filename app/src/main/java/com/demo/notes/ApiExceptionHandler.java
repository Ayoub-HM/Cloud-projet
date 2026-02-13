package com.demo.notes;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .findFirst()
        .map(fieldError -> {
          String defaultMessage = fieldError.getDefaultMessage();
          if (defaultMessage == null || defaultMessage.isBlank()) {
            return fieldError.getField() + " is invalid";
          }
          return fieldError.getField() + " " + defaultMessage;
        })
        .orElse("invalid request body");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "invalid request body"));
  }
}
