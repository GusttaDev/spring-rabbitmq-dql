package com.gusttadev.orderproducer.domain.order;

public record ItemEvent(String product, int quantity, double price) {
}