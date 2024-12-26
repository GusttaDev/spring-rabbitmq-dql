package com.gusttadev.orderconsumer.config;

import com.gusttadev.orderconsumer.constants.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${spring.rabbit.concurrent-consumers}")
    private Integer concurrentConsumers;

    @Value("${spring.rabbit.max-concurrent-consumers}")
    private Integer maxConcurrentConsumers;

    @Value("${spring.rabbit.prefetch-count}")
    private Integer prefetchCount;

    @Value("${spring.rabbit.max-attempts}")
    private Integer maxAttempts;

    // Configuração para Order Queue e DLQ (mantém o comportamento atual)
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(Constants.ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", Constants.ORDER_QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue orderDlq() {
        return new Queue(Constants.ORDER_QUEUE_DLQ, true);
    }

    @Bean
    public Exchange orderExchange() {
        return new TopicExchange(Constants.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with("order.#").noargs();
    }

    // Configuração para Customer Queue e DLQ
    @Bean
    public Queue customerQueue() {
        return QueueBuilder.durable(Constants.CUSTOMER_QUEUE).build();
    }

    @Bean
    public Queue customerDlq() {
        return new Queue(Constants.CUSTOMER_QUEUE_DLQ, true);
    }

    @Bean
    public Exchange customerExchange() {
        return new TopicExchange(Constants.CUSTOMER_EXCHANGE, true, false);
    }

    @Bean
    public Binding customerBinding() {
        return BindingBuilder.bind(customerQueue()).to(customerExchange()).with("customer.#").noargs();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter()); // Adicionando o conversor aqui
        return rabbitTemplate;
    }

    // Consumer Factory para Order (mantendo comportamento de envio automático para DLQ)
    @Bean
    public SimpleRabbitListenerContainerFactory orderRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setPrefetchCount(prefetchCount);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer()) // Envia para DLQ após 3 tentativas
                .build());
        return factory;
    }

    // Consumer Factory para Customer (manter controle manual para DLQ)
    @Bean
    public SimpleRabbitListenerContainerFactory customerRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setPrefetchCount(prefetchCount);
        factory.setDefaultRequeueRejected(false); // Não reprocessar diretamente
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
