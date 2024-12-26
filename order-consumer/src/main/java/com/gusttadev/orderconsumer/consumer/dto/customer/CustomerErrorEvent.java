package com.gusttadev.orderconsumer.consumer.dto.customer;

import java.util.List;

public record CustomerErrorEvent(CustomerEvent customerEvent, List<String> errors) {
}
