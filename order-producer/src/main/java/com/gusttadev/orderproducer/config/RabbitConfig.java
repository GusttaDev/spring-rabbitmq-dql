package com.gusttadev.orderproducer.config;

import com.gusttadev.orderproducer.constants.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue orderQueue() {
        // Incluindo a configuração do x-dead-letter-exchange
        return QueueBuilder.durable(Constants.ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", Constants.ORDER_QUEUE_DLQ)
                .build();
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(Constants.ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with("order.routingKey");
    }


    @Bean
    public Queue customerQueue() {
        return QueueBuilder.durable(Constants.CUSTOMER_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange customerExchange() {
        return new TopicExchange(Constants.CUSTOMER_EXCHANGE);
    }

    @Bean
    public Binding customerBinding() {
        return BindingBuilder.bind(customerQueue()).to(customerExchange()).with("customer.routingKey");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
