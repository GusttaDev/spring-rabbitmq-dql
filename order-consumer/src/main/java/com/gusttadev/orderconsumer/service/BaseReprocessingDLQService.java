package com.gusttadev.orderconsumer.service;

import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseReprocessingDLQService<T> {

    protected final Logger logger;
    protected final RabbitTemplate rabbitTemplate;

    protected BaseReprocessingDLQService(Logger logger, RabbitTemplate rabbitTemplate) {
        this.logger = logger;
        this.rabbitTemplate = rabbitTemplate;
    }

    protected abstract String getDlqQueue();
    protected abstract String getExchange();
    protected abstract String getRoutingKey();
    protected abstract String getRetryHeader();
    protected abstract T convertMessageToEvent(Message message);
    protected abstract void saveErrorToDatabase(T event);

    public void reprocessDlqMessages() {
        Message message = rabbitTemplate.receive(getDlqQueue());

        if (message != null) {
            Integer retryHeader = message.getMessageProperties().getHeader(getRetryHeader());
            retryHeader = (retryHeader == null) ? 0 : retryHeader;

            logger.info("Reprocessando mensagem da DLQ");

            try {
                T event = convertMessageToEvent(message); // Conversão da mensagem
                if (retryHeader < 1) {
                    int tryCount = retryHeader + 1;
                    Map<String, Object> updatedHeaders = new HashMap<>();
                    updatedHeaders.put(getRetryHeader(), tryCount);

                    final MessagePostProcessor messagePostProcessor = msg -> {
                        MessageProperties props = msg.getMessageProperties();
                        updatedHeaders.forEach(props::setHeader);
                        return msg;
                    };

                    logger.info("Reenviando evento para a fila principal");
                    rabbitTemplate.convertAndSend(getExchange(), getRoutingKey(), event, messagePostProcessor);
                } else {
                    logger.error("Reprocessamento falhou, salvando na base de dados");
                    saveErrorToDatabase(event); // Salvar erro no banco de dados
                }
            } catch (Exception e) {
                logger.error("Erro ao reprocessar a mensagem: {}", e.getMessage());
            }
        }
    }
}
