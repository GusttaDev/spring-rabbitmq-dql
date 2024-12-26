package com.gusttadev.orderconsumer.consumer.dto.order;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record OrderItemEvent(
        @NotBlank(message = "O campo [product] não pode ser vazio ou nulo.") String product,
        @Min(value = 1, message = "O campo [quantity] não pode ser vazio ou nulo.") int quantity,
        @NotNull(message = "O campo [price] não pode ser nulo.")
        @DecimalMin(value = "0.01", message = "O campo [price] deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O campo [price] não é valido.") BigDecimal price) {
}