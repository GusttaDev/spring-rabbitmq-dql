package com.gusttadev.orderconsumer.exception;

public class CustomerProcessingException extends RuntimeException {
    private final String value;

    public CustomerProcessingException(String value) {
        super("Erro no processamento do clinete de cpf = "+value);
        this.value = value;
    }

    public String getResult() {
        return value;
    }
}