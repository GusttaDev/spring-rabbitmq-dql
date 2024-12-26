package com.gusttadev.orderproducer.publisher;

import com.gusttadev.orderproducer.constants.Constants;
import com.gusttadev.orderproducer.domain.order.OrderEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrder(OrderEvent orderEvent){
        rabbitTemplate.convertAndSend(Constants.ORDER_EXCHANGE, Constants.ORDER_ROUTING_KEY, orderEvent);
    }
}
