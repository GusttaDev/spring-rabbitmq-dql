package com.gusttadev.orderconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gusttadev.orderconsumer.constants.Constants;
import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerErrorEvent;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CustomerReprocessingService extends BaseReprocessingDLQService<CustomerErrorEvent> {

    private final ObjectMapper objectMapper;
    private final CustomerErrorService customerErrorService;

    public CustomerReprocessingService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, CustomerErrorService customerErrorService) {
        super(LoggerFactory.getLogger(CustomerReprocessingService.class), rabbitTemplate);
        this.objectMapper = objectMapper;
        this.customerErrorService = customerErrorService;
    }

    @Scheduled(fixedDelay = 30000) // Executa a cada 30s
    public void scheduleReprocessing() {
        reprocessDlqMessages();
    }

    @Override
    protected String getDlqQueue() {
        return Constants.CUSTOMER_QUEUE_DLQ;
    }

    @Override
    protected String getExchange() {
        return Constants.CUSTOMER_EXCHANGE;
    }

    @Override
    protected String getRoutingKey() {
        return Constants.CUSTOMER_ROUTING_KEY_VALUE;
    }

    @Override
    protected String getRetryHeader() {
        return "x-dlq-retry";
    }

    @Override
    protected CustomerErrorEvent convertMessageToEvent(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), CustomerErrorEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter mensagem para CustomerErrorEvent", e);
        }
    }

    @Override
    protected void saveErrorToDatabase(CustomerErrorEvent event) {
        customerErrorService.save(event);
    }
}
