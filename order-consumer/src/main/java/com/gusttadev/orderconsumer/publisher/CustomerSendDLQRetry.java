package com.gusttadev.orderconsumer.publisher;

import com.gusttadev.orderconsumer.constants.Constants;
import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerErrorEvent;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerSendDLQRetry {

    private final RabbitTemplate rabbitTemplate;

    public CustomerSendDLQRetry(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToDLQ(CustomerErrorEvent customerErrorEvent, Integer retryCount) {
        MessagePostProcessor messagePostProcessor = getMessagePostProcessor(retryCount);
        rabbitTemplate.convertAndSend(Constants.CUSTOMER_QUEUE_DLQ, customerErrorEvent, messagePostProcessor);
    }

    private static MessagePostProcessor getMessagePostProcessor(Integer retryCount) {
        return msg -> {
            MessageProperties props = msg.getMessageProperties();
            props.setHeader("x-dlq-retry", retryCount);
            return msg;
        };
    }
}