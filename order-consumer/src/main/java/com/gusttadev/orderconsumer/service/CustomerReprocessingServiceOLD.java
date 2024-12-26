package com.gusttadev.orderconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gusttadev.orderconsumer.constants.Constants;
import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerErrorEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Deprecated
public class CustomerReprocessingServiceOLD {
    private final Logger logger = LoggerFactory.getLogger(CustomerReprocessingServiceOLD.class);
    public static final String X_RETRY_HEADER = "x-dlq-retry";

    private final RabbitTemplate rabbitTemplate;
    private final CustomerErrorService customerErrorService;
    private final ObjectMapper objectMapper;

    //@Scheduled(fixedDelay = 30000) // Executa a cada 60s
    public void reprocessDlqMessages() {
        Message message = rabbitTemplate.receive(Constants.CUSTOMER_QUEUE_DLQ);

        if (message != null) {
            Integer retryHeader = message.getMessageProperties().getHeader(X_RETRY_HEADER);
            retryHeader = (retryHeader == null) ? 0 : retryHeader;

            logger.info("Reprocessando mensagem da DLQ");

            try {
                CustomerErrorEvent customerErrorEvent = convertMessageToCustomer(message); // Conversão da mensagem
                if (retryHeader < 1) {
                    int tryCount = retryHeader + 1;
                    Map<String, Object> updatedHeaders = new HashMap<>();
                    updatedHeaders.put(X_RETRY_HEADER, tryCount);

                    final MessagePostProcessor messagePostProcessor = msg -> {
                        MessageProperties props = msg.getMessageProperties();
                        updatedHeaders.forEach(props::setHeader);
                        return msg;
                    };

                    logger.info("Reenviando cliente para a fila principal");
                    this.rabbitTemplate.convertAndSend(Constants.CUSTOMER_EXCHANGE, Constants.CUSTOMER_ROUTING_KEY_VALUE, customerErrorEvent.customerEvent(), messagePostProcessor);
                } else {
                    logger.error("Reprocessamento falhou, salvando na base de dados");
                    saveErrorToDatabase(customerErrorEvent); // Método para salvar o erro
                }
            } catch (Exception e) {
                logger.error("Erro ao reprocessar a mensagem: {}", e.getMessage());
            }
        }
    }

    private CustomerErrorEvent convertMessageToCustomer(Message message) {
        try {
            String json = new String(message.getBody());
            return objectMapper.readValue(json, CustomerErrorEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter mensagem para objeto cliente", e);
        }
    }
    private void saveErrorToDatabase(CustomerErrorEvent customerErrorEvent) {
        customerErrorService.save(customerErrorEvent);
    }
}