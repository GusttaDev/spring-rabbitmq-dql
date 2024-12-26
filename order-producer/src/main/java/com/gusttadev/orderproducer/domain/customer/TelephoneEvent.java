package com.gusttadev.orderproducer.domain.customer;

import com.gusttadev.orderproducer.domain.enums.TelephoneType;

public record TelephoneEvent(int ddd, String number, TelephoneType type) {
}