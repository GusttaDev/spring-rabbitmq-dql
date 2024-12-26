package com.gusttadev.orderproducer.controller;

import com.gusttadev.orderproducer.domain.customer.CustomerEvent;
import com.gusttadev.orderproducer.domain.order.OrderEvent;
import com.gusttadev.orderproducer.publisher.CustomerPublisher;
import com.gusttadev.orderproducer.publisher.OrderPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerPublisher customerPublisher;

    @PostMapping("/send-customer")
    public ResponseEntity<String> sendOrder(@RequestBody CustomerEvent customerEvent) {
        customerPublisher.sendCustomer(customerEvent);
        return ResponseEntity.ok("Customer sent successfully!");
    }
}
