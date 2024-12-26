package com.gusttadev.orderconsumer.consumer.dto.customer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddressEvent(
        @NotBlank(message = "O campo [zipCode] não pode ser vazio ou nulo.") String zipCode,
        @Min(value = 1, message = "O campo [number] não pode ser vazio ou nulo.") int number,
        String complement, String street, String neighborhood, String recipient
) {}
