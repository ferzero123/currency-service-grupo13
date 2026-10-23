package com.cibertec.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ConversionRequest(
        @NotBlank(message = "El campo 'from' no puede estar vacío")
        String from,

        @NotBlank(message = "El campo 'to' no puede estar vacío")
        String to,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal amount
) {
}