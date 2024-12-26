package com.gusttadev.orderconsumer.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gusttadev.orderconsumer.constants.Constants;
import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerErrorEvent;
import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerEvent;
import com.gusttadev.orderconsumer.exception.CustomerProcessingException;
import com.gusttadev.orderconsumer.publisher.CustomerSendDLQRetry;
import com.gusttadev.orderconsumer.service.CustomerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomerConsumer {

    private final Logger logger = LoggerFactory.getLogger(CustomerConsumer.class);
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;
    private final CustomerSendDLQRetry customerSendDLQRetry;
    private final Validator validator;
    int retryCount = 0;


    @RabbitListener(queues = Constants.CUSTOMER_QUEUE, containerFactory = "customerRabbitListenerContainerFactory")
    public void consumeCustomer(Message message) {
            CustomerEvent customerEvent = convertMessageToCustomer(message);
            List<String> errors = new ArrayList<>();
        try {
            Set<ConstraintViolation<CustomerEvent>> violations = validator.validate(customerEvent);
            errors = violations.stream()
                    .map(ConstraintViolation::getMessage).toList();

            Integer retryHeader = message.getMessageProperties().getHeader("x-dlq-retry");
            retryCount = (retryHeader != null) ? retryHeader : 0;

            if (!errors.isEmpty()) {
                throw new CustomerProcessingException(customerEvent.cpf());
            } else {
                customerService.save(customerEvent);
            }
        }catch (CustomerProcessingException ce){
            logger.error("Listando erros de validação. Error = {}", errors);
        }catch (Exception e){
            CustomerErrorEvent customerErrorEvent = buildCustomerError(customerEvent, errors);
            // Chamando manualmente o serviço de envio para DLQ
            customerSendDLQRetry.sendToDLQ(customerErrorEvent, retryCount);
        }
    }

    private CustomerErrorEvent buildCustomerError(CustomerEvent customerEvent, List<String> errors) {
        return new CustomerErrorEvent(customerEvent, errors);
    }

    private CustomerEvent convertMessageToCustomer(Message message) {
        try {
            String json = new String(message.getBody());

            JsonNode rootNode = objectMapper.readTree(json);
            if (rootNode.has("errors")) {
                json = extractCustomerEventFromJson(rootNode);
            }

            return objectMapper.readValue(json, CustomerEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter mensagem para objeto Customer", e);
        }
    }

    private String extractCustomerEventFromJson(JsonNode rootNode) {
        try {
            JsonNode customerEventNode = rootNode.get("customerEvent");
            return objectMapper.writeValueAsString(customerEventNode);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair customerEvent do JSON", e);
        }
    }
}