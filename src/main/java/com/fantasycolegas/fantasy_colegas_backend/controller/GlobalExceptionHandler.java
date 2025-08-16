package com.fantasycolegas.fantasy_colegas_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador de excepciones global para la aplicación.
 * <p>
 * Captura y gestiona excepciones comunes de Spring, como errores de validación,
 * para devolver respuestas HTTP estandarizadas y con mensajes claros.
 * </p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción {@link MethodArgumentNotValidException} que se lanza
     * cuando los datos de un DTO con {@code @Valid} no son válidos.
     *
     * @param ex La excepción lanzada por Spring.
     * @return Una {@link ResponseEntity} con el estado {@code 400 Bad Request}
     * y los errores de validación en el cuerpo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
