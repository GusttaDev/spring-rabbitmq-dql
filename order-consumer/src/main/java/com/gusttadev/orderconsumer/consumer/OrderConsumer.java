package com.gusttadev.orderconsumer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gusttadev.orderconsumer.constants.Constants;
import com.gusttadev.orderconsumer.consumer.dto.order.OrderEvent;
import com.gusttadev.orderconsumer.exception.OrderProcessingException;
import com.gusttadev.orderconsumer.service.OrderService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    int countAttempts = 0;
    @RabbitListener(queues = Constants.ORDER_QUEUE, containerFactory = "orderRabbitListenerContainerFactory")
    public void consumeOrder(Message message) {
        OrderEvent orderEvent = convertMessageToOrder(message);

        List<String> errors = validationOrderEventFields(orderEvent);

        if (!errors.isEmpty()) {
            countAttempts++;
            if(countAttempts == 3){
                logger.error("Listando erros de validação. Errror = {}", errors);
                countAttempts = 0;
            }

            throw new OrderProcessingException(orderEvent.orderId());
        }

        orderService.save(orderEvent);
    }

    private List<String> validationOrderEventFields(OrderEvent orderEvent) {
        Set<ConstraintViolation<OrderEvent>> violations = validator.validate(orderEvent);
        return violations.stream()
                .map(ConstraintViolation::getMessage).toList();
    }

    private OrderEvent convertMessageToOrder(Message message) {
        try {
            String json = new String(message.getBody());
            return objectMapper.readValue(json, OrderEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter mensagem para objeto Order", e);
        }
    }
}
