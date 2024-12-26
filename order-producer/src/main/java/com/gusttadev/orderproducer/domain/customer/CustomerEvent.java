package com.gusttadev.orderproducer.domain.customer;

import com.gusttadev.orderproducer.domain.enums.Gender;

import java.util.List;

public record CustomerEvent(String firstName, String lastName, String cpf, Gender gender,
                            String dateOfBirth, List<TelephoneEvent> phones, List<AddressEvent> addresses) {
}
