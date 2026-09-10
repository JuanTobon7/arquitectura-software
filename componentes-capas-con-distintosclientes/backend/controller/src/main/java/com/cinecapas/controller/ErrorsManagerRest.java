package com.cinecapas.controller;

import com.cinecapas.application.exceptions.AccessNotGranted;
import com.cinecapas.application.exceptions.DuplicatedEmailException;
import com.cinecapas.application.exceptions.InvalidRequestException;
import com.cinecapas.application.exceptions.NotAviableSeatException;
import com.cinecapas.application.exceptions.NotFoundReservationException;
import com.cinecapas.application.exceptions.NotFoundScreeningException;
import com.cinecapas.application.exceptions.ReservationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErrorsManagerRest {

    @ExceptionHandler({NotFoundScreeningException.class, NotFoundReservationException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> noEncontrado(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler({NotAviableSeatException.class, DuplicatedEmailException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflicto(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(AccessNotGranted.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> noAutorizado(AccessNotGranted ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class, ReservationException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> peticionInvalida(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Petición inválida");
        return Map.of("error", detalle);
    }
}
