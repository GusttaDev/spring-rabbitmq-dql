package com.gusttadev.orderproducer.publisher;

import com.gusttadev.orderproducer.constants.Constants;
import com.gusttadev.orderproducer.domain.customer.CustomerEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerPublisher {

    private final RabbitTemplate rabbitTemplate;

    public CustomerPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendCustomer(CustomerEvent customerEvent){
        rabbitTemplate.convertAndSend(Constants.CUSTOMER_EXCHANGE, Constants.CUSTOMER_ROUTING_KEY, customerEvent);
    }
}
