package com.gusttadev.orderproducer.controller;

import com.gusttadev.orderproducer.domain.order.OrderEvent;
import com.gusttadev.orderproducer.publisher.OrderPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderPublisher orderPublisher;

    public OrderController(OrderPublisher orderPublisher) {
        this.orderPublisher = orderPublisher;
    }

    @PostMapping("/send-order")
    public ResponseEntity<String> sendOrder(@RequestBody OrderEvent order) {
        orderPublisher.sendOrder(order);
        return ResponseEntity.ok("Order sent successfully!");
    }
}
