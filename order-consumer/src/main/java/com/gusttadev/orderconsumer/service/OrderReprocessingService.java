package com.gusttadev.orderconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gusttadev.orderconsumer.constants.Constants;
import com.gusttadev.orderconsumer.consumer.dto.order.OrderEvent;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderReprocessingService extends BaseReprocessingDLQService<OrderEvent> {

    private final ObjectMapper objectMapper;
    private final OrderErrorService orderErrorService;

    public OrderReprocessingService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, OrderErrorService orderErrorService) {
        super(LoggerFactory.getLogger(OrderReprocessingService.class), rabbitTemplate);
        this.objectMapper = objectMapper;
        this.orderErrorService = orderErrorService;
    }

    @Scheduled(fixedDelay = 60000) // Executa a cada 60s
    public void scheduleReprocessing() {
        reprocessDlqMessages();
    }

    @Override
    protected String getDlqQueue() {
        return Constants.ORDER_QUEUE_DLQ;
    }

    @Override
    protected String getExchange() {
        return Constants.ORDER_EXCHANGE;
    }

    @Override
    protected String getRoutingKey() {
        return Constants.ORDER_ROUTING_KEY_VALUE;
    }

    @Override
    protected String getRetryHeader() {
        return "x-dlq-retry";
    }

    @Override
    protected OrderEvent convertMessageToEvent(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), OrderEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter mensagem para OrderEvent", e);
        }
    }

    @Override
    protected void saveErrorToDatabase(OrderEvent event) {
        orderErrorService.save(event);
    }

}
