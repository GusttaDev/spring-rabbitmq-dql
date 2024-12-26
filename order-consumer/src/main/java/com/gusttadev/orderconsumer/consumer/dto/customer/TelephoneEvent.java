package com.gusttadev.orderconsumer.consumer.dto.customer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TelephoneEvent(
        @Min(value = 1, message = "O campo [ddd] não pode ser vazio ou nulo.") int ddd,
        @NotBlank(message = "O campo [number] não pode ser vazio ou nulo.") String number,
        String type
) {}
