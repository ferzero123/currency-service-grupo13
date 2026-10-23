package com.cibertec.exception;

import com.cibertec.controller.CurrencyController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = CurrencyController.class)
public class CountryCurrencyExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleCountryApiError(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "mensaje", ex.getReason() == null ? "Error consultando el pais." : ex.getReason(),
                "estado", ex.getStatusCode().value()
        ));
    }
}
