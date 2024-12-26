# Artefatos da Aplicação

## 1. **RabbitConfig.java**

Essa classe configura a comunicação com o RabbitMQ, que é um sistema de filas. Vamos explicar cada parte dela:
```
@Configuration
public class RabbitConfig {
```
- `@Configuration`: Esta anotação marca a classe como uma configuração do Spring, onde beans serão definidos.

```
@Bean
public Queue orderQueue() {
return QueueBuilder.durable(Constants.ORDER_QUEUE)
.withArgument("x-dead-letter-exchange", "")
.withArgument("x-dead-letter-routing-key", Constants.ORDER_QUEUE_DLQ)
.build();
}
```

- `@Bean`: Define que o método orderQueue retorna um bean do tipo Queue que será gerenciado pelo Spring.
- `QueueBuilder.durable(...)`: Cria uma fila durável (persiste no RabbitMQ). O nome da fila é order-queue.
- `withArgument("x-dead-letter-exchange", "")`: Especifica que a fila orderQueue usará um Dead Letter Exchange (DLX). A fila de DLX ajuda a tratar mensagens que não puderam ser processadas.
- `withArgument("x-dead-letter-routing-key")`, Constants.ORDER_QUEUE_DLQ): Define a chave de roteamento para mensagens que falham (essas mensagens serão enviadas para a fila de DLQ).

```
@Bean
public TopicExchange orderExchange() {
return new TopicExchange(Constants.ORDER_EXCHANGE);
}
```

- `TopicExchange`: Cria uma exchange do tipo "topic". Ela roteia as mensagens para filas com base em padrões de chave de roteamento.
- `Constants.ORDER_EXCHANGE`: Define o nome da exchange como order-exchange.

```
@Bean
public Binding orderBinding() {
return BindingBuilder.bind(orderQueue()).to(orderExchange()).with("order.routingKey");
}
```

- `BindingBuilder.bind(...)`: Cria uma associação (binding) entre a fila orderQueue e a exchange orderExchange. A chave de roteamento usada é order.routingKey.

```
@Bean
public Queue customerQueue() {
return QueueBuilder.durable(Constants.CUSTOMER_QUEUE)
.build();
}
```

- Cria uma fila durável para customerQueue.

```
@Bean
public TopicExchange customerExchange() {
return new TopicExchange(Constants.CUSTOMER_EXCHANGE);
}
```

- Cria uma exchange do tipo "topic" chamada customer-exchange.

```
@Bean
public Binding customerBinding() {
return BindingBuilder.bind(customerQueue()).to(customerExchange()).with("customer.routingKey");
}
```

- Cria o binding entre a fila customerQueue e a exchange customerExchange, usando a chave de roteamento customer.routingKey.

```
@Bean
public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
return new Jackson2JsonMessageConverter();
}
```

- `Jackson2JsonMessageConverter`: Converte os objetos Java em mensagens JSON e vice-versa, quando enviando/recebendo mensagens via RabbitMQ.

## 2. **Constants.java**

A classe Constants contém constantes usadas no código, como os nomes das filas, exchanges e chaves de roteamento.

```
public class Constants {
   public static final String ORDER_QUEUE = "order-queue";
   public static final String ORDER_QUEUE_DLQ = "order-queue-dlq";
   public static final String ORDER_EXCHANGE = "order-exchange";
   public static final String ORDER_ROUTING_KEY = "order.routingKey";
   public static final String CUSTOMER_QUEUE = "customer-queue";
   public static final String CUSTOMER_QUEUE_DLQ = "customer-queue-dlq";
   public static final String CUSTOMER_EXCHANGE = "customer-exchange";
   public static final String CUSTOMER_ROUTING_KEY = "customer.routingKey";
}
```

Essas constantes são usadas em várias partes do código para garantir que o nome de filas, exchanges e rotas sejam consistentes.

## 3. **Controllers**

Os controladores expõem endpoints REST para enviar mensagens (de cliente e de pedido).

Classe **CustomerController**

```
@RestController
@RequiredArgsConstructor
public class CustomerController {
 private final CustomerPublisher customerPublisher;

@PostMapping("/send-customer")
public ResponseEntity<String> sendOrder(@RequestBody CustomerEvent customerEvent) {
   customerPublisher.sendCustomer(customerEvent);
   return ResponseEntity.ok("Customer sent successfully!");
   }
}
```

- `@RestController`: Define o controlador que irá lidar com requisições HTTP e enviar respostas.
- `@RequiredArgsConstructor`: Gera automaticamente um construtor para a classe, injetando o CustomerPublisher.
- `sendOrder(@RequestBody CustomerEvent customerEvent)`: Método que recebe um objeto CustomerEvent no corpo da requisição e chama o método sendCustomer do customerPublisher para enviar o evento de cliente.

Classe **OrderController**

```
@RestController
public class OrderController {
private final OrderPublisher orderPublisher;

public OrderController(OrderPublisher orderPublisher) {
   this.orderPublisher = orderPublisher;
}

@PostMapping("/send-order")
public ResponseEntity<String> sendOrder(@RequestBody OrderEvent order) {
   orderPublisher.sendOrder(order);
   return ResponseEntity.ok("Order sent successfully!");
   }
}
```
## 4. **Domain**

Contém as definições de eventos (modelos de dados) que serão enviados via RabbitMQ.

**CustomerEvent** e **AddressEvent**

```public record AddressEvent(String zipCode, int number, String complement, String street, String neighborhood, String recipient) {}```

- `AddressEvent`: Define as informações de um endereço. record é uma nova funcionalidade do Java que define classes imutáveis.

```
public record CustomerEvent(String firstName, String lastName, String cpf, Gender gender,
String dateOfBirth, List<TelephoneEvent> phones, List<AddressEvent> addresses) {}
```

- `CustomerEvent`: Representa os dados de um cliente, incluindo nome, CPF, telefone e endereços.

**OrderEvent** e **ItemEvent**

```
public record ItemEvent(String product, int quantity, double price) {}
```

- `ItemEvent`: Representa um item de um pedido, com informações sobre o produto, quantidade e preço.

```
public record OrderEvent(int orderId, int customerId, List<ItemEvent> items) {}
```

- `OrderEvent`: Representa um pedido com um ID de pedido, ID de cliente e uma lista de itens (ItemEvent). 

## 5. **Publisher**

As classes **CustomerPublisher** e **OrderPublisher** são responsáveis por enviar os eventos para as filas RabbitMQ.

Classe **CustomerPublisher**

```
@Component
public class CustomerPublisher {
private final RabbitTemplate rabbitTemplate;

public CustomerPublisher(RabbitTemplate rabbitTemplate) {
   this.rabbitTemplate = rabbitTemplate;
}

public void sendCustomer(CustomerEvent customerEvent) {
   rabbitTemplate.convertAndSend(Constants.CUSTOMER_EXCHANGE, Constants.CUSTOMER_ROUTING_KEY, customerEvent);
   }
}
```

- `@Component`: A classe será registrada como um bean do Spring e pode ser injetada em outros componentes.
- `rabbitTemplate.convertAndSend(...)`: Converte o CustomerEvent para uma mensagem e a envia para a exchange customer-exchange, usando a chave de roteamento customer.routingKey.

- Classe **OrderPublisher**

```
@Component
public class OrderPublisher {
private final RabbitTemplate rabbitTemplate;

public OrderPublisher(RabbitTemplate rabbitTemplate) {
   this.rabbitTemplate = rabbitTemplate;
}

public void sendOrder(OrderEvent orderEvent) {
   rabbitTemplate.convertAndSend(Constants.ORDER_EXCHANGE, Constants.ORDER_ROUTING_KEY, orderEvent);
   }
}
```