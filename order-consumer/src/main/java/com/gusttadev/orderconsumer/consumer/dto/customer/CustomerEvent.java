package com.gusttadev.orderconsumer.consumer.dto.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CustomerEvent(
        @NotBlank(message = "O campo [firstName] não pode ser vazio ou nulo.") String firstName,
        @NotBlank(message = "O campo [lastName] não pode ser vazio ou nulo.") String lastName,
        @NotBlank(message = "O campo [cpf] não pode ser vazio ou nulo.") String cpf,
        String gender,
        String dateOfBirth,
        @NotEmpty(message = "A lista de [telephones] não pode ser vazia.") List<@Valid TelephoneEvent> phones,
        @NotEmpty(message = "A lista de [addresses] não pode ser vazia.") List<@Valid AddressEvent> addresses
) {}
