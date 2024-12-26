package com.gusttadev.orderconsumer.exception;

public class OrderProcessingException extends RuntimeException {
    private final Long value;

    public OrderProcessingException(Long value) {
        super("Erro no processamento do pedido de id = "+value);
        this.value = value;
    }

    public Long getResult() {
        return value;
    }
}
