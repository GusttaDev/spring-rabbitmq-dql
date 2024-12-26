package com.gusttadev.orderproducer.domain.customer;

public record AddressEvent(String zipCode, int number, String complement, String street, String neighborhood, String recipient) {
}