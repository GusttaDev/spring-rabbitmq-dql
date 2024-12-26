package com.gusttadev.orderproducer.domain.order;

import java.util.List;

public record OrderEvent(int orderId, int customerId, List<ItemEvent> items) {
}
