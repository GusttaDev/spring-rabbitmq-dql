package com.gusttadev.orderconsumer.consumer.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderEvent(
        @NotNull(message = "O campo [orderId] não pode ser nulo.") Long orderId,
        @NotNull(message = "O campo [customerId] não pode ser vazio ou nulo.") Long customerId,
        @NotEmpty(message = "A lista de [items] não pode ser vazia.") List<@Valid OrderItemEvent> items) {
}
