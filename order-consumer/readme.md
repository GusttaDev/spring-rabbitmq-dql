# Artefatos da Aplicação

## 1. **ObjectMapperConfig.java**

A classe `ObjectMapperConfig` configura um `ObjectMapper`, que é uma biblioteca do **Jackson** usada para conversão entre objetos Java e JSON. Esse **bean** (`@Bean`) cria e configura o `ObjectMapper` para ser usado na aplicação.

### Explicação do código:
- `@Configuration`: Anotação que indica que essa classe contém configurações para o Spring.
- `@Bean`: Define um método que cria e configura um bean que será gerenciado pelo Spring.
- `objectMapper()`: Este método cria uma instância do `ObjectMapper` e aplica configurações personalizadas, como:
    - `objectMapper.setTimeZone(TimeZone.getDefault())`: Configura o `ObjectMapper` para usar o fuso horário padrão do sistema.
    - `objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)`: Configura o `ObjectMapper` para não lançar exceções quando encontrar propriedades desconhecidas durante a desserialização.
    - `objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)`: Configura para tratar strings vazias como `null` ao desserializar.

Esse `ObjectMapper` será injetado onde necessário para conversões de **JSON**.

---

## 2. **RabbitConfig.java**

Esta classe configura as filas, trocas e bindings de mensagens para o **RabbitMQ**, que é uma fila de mensagens usada para comunicação assíncrona entre sistemas.

### Explicação do código:
- `@Configuration`: Indica que é uma classe de configuração para o Spring.
- `@Value("${...}")`: Anotação para injetar valores do arquivo de configuração (como `application.properties` ou `application.yml`).
    - Exemplo: `@Value("${spring.rabbit.concurrent-consumers}")` injeta o valor da propriedade `spring.rabbit.concurrent-consumers`.

A classe contém a configuração para duas filas principais: **orderQueue** e **customerQueue**, além de suas **Dead Letter Queues (DLQ)**, que são usadas para mensagens que não podem ser processadas.

### 1. **Filas e Trocas**:
- `orderQueue`: A fila principal para pedidos (**Orders**).
- `orderDlq`: A fila DLQ associada ao `orderQueue`.
- `customerQueue`: A fila principal para clientes (**Customers**).
- `customerDlq`: A fila DLQ associada ao `customerQueue`.

### 2. **Troca (Exchange)**:
- `orderExchange` e `customerExchange` são **TopicExchange**, usados para distribuir mensagens com base em tópicos.

### 3. **Binding**:
- `orderBinding`: Vincula a fila `orderQueue` à troca `orderExchange` usando o tópico `order.#`, o que significa que ela receberá mensagens com qualquer chave de roteamento que comece com `order.`.
- `customerBinding`: Vincula a fila `customerQueue` à troca `customerExchange` com o tópico `customer.#`.

### 4. **RabbitTemplate**:
- `rabbitTemplate`: Cria um template que facilita a publicação de mensagens no **RabbitMQ**. Ele usa um conversor (**Jackson2JsonMessageConverter**) para converter mensagens para o formato **JSON**.

### 5. **Listener Container Factory**:
- `orderRabbitListenerContainerFactory` e `customerRabbitListenerContainerFactory` são fábricas de listeners que configuram como as mensagens serão consumidas. A configuração inclui:
    - O número máximo de consumidores concorrentes.
    - Quantidade de mensagens por consumidor (**prefetch**).
    - Tratamento de falhas.
- `RetryInterceptorBuilder` na configuração de `orderRabbitListenerContainerFactory` permite que o consumidor tente processar a mensagem até um número máximo de tentativas (`maxAttempts`) antes de enviá-la para a DLQ.

---

## 3. **ValidatorConfig.java**

Esta classe configura o **Validator**, usado para validar os dados das mensagens recebidas, conforme as anotações de validação no modelo de dados.

### Explicação do código:
- `@Bean`: Cria um **bean** do tipo **Validator**, que será utilizado no processo de validação de objetos, como `CustomerEvent` e `OrderEvent`.
- O **Validator** é configurado para usar o **ParameterMessageInterpolator** para interpolar mensagens de erro de validação.

---

## 4. **Constants.java**

Essa classe contém as **constantes** que definem os nomes das filas, trocas e chaves de roteamento usadas no **RabbitMQ**.

### Explicação do código:
- **Constantes de filas**: Definem os nomes das filas usadas para comunicação (ex.: `order-queue`, `customer-queue`).
- **Constantes de DLQ**: Definem as filas de **Dead Letter** (DLQ) (ex.: `order-queue-dlq`, `customer-queue-dlq`).
- **Constantes de trocas**: Definem os nomes das trocas (ex.: `order-exchange`, `customer-exchange`).
- **Constantes de chaves de roteamento**: Definem os valores das chaves usadas para rotear as mensagens (ex.: `order.created`, `customer.created`).

Essas constantes evitam problemas de hardcoding de strings no código, proporcionando mais manutenção e clareza.

---

Os consumidores `CustomerConsumer` e `OrderConsumer` são responsáveis por processar eventos relacionados a clientes e pedidos, respectivamente, que são recebidos de uma fila no RabbitMQ. Vamos analisar o que cada um faz, passo a passo, em seus respectivos métodos.

## 5. **CustomerConsumer**
O `CustomerConsumer` é um consumidor que processa eventos de cliente. Aqui estão os principais pontos de sua implementação:

1. **Injeção de Dependências**
    - **Logger**: Usado para registrar logs, especialmente para erros e mensagens de validação.
    - **CustomerService**: Serviço que lida com a persistência de eventos de clientes.
    - **ObjectMapper**: Ferramenta usada para converter entre objetos Java e JSON.
    - **CustomerSendDLQRetry**: Serviço responsável por enviar eventos de erro para a Dead Letter Queue (DLQ) se o processamento falhar.
    - **Validator**: Usado para validar o `CustomerEvent` de acordo com as restrições definidas na classe.

2. **Método `consumeCustomer`**
    - O método é anotado com `@RabbitListener`, indicando que ele escuta a fila `CUSTOMER_QUEUE` no RabbitMQ.
    - O método recebe uma `Message` do RabbitMQ e converte a mensagem para um objeto `CustomerEvent` por meio do método `convertMessageToCustomer`.
    - Após a conversão, o evento é validado usando o `Validator`. Se houver erros de validação, esses erros são registrados e o processamento é interrompido, lançando uma exceção personalizada `CustomerProcessingException`.
    - Se não houver erros, o evento de cliente é salvo no banco de dados utilizando o `customerService.save`.

3. **Exceções e Reenvio de Mensagens**
    - Caso ocorra uma exceção de validação (`CustomerProcessingException`), ela é capturada e os erros são registrados.
    - Para outros tipos de exceções, o evento de erro (`CustomerErrorEvent`) é criado e enviado para a DLQ usando o serviço `customerSendDLQRetry.sendToDLQ`.
    - A contagem de tentativas de reenvio é controlada com um cabeçalho de mensagem `x-dlq-retry`.

4. **Métodos auxiliares**
    - **`buildCustomerError`**: Cria um evento de erro (`CustomerErrorEvent`) com base no evento do cliente e na lista de erros encontrados durante a validação.
    - **`convertMessageToCustomer`**: Converte a mensagem do RabbitMQ para um objeto `CustomerEvent` usando o `ObjectMapper`. Se a mensagem contiver erros, ela extrai a parte relevante do JSON.
    - **`extractCustomerEventFromJson`**: Extrai o conteúdo do `customerEvent` de um JSON maior.

---

## 6. **OrderConsumer**
O `OrderConsumer` é um consumidor responsável pelo processamento de eventos de pedidos. Vamos analisar a lógica:

1. **Injeção de Dependências**
    - **Logger**: Usado para registrar logs, especialmente para erros de validação.
    - **OrderService**: Serviço que lida com a persistência de eventos de pedidos.
    - **ObjectMapper**: Converte objetos Java para JSON e vice-versa.
    - **Validator**: Usado para validar os campos do `OrderEvent` antes de processá-lo.

2. **Método `consumeOrder`**
    - O método é anotado com `@RabbitListener`, indicando que ele escuta a fila `ORDER_QUEUE` no RabbitMQ.
    - Ele recebe uma `Message` do RabbitMQ e converte a mensagem para um objeto `OrderEvent` usando o método `convertMessageToOrder`.
    - O evento de pedido é validado com o `Validator`. Se houver erros de validação, o contador `countAttempts` é incrementado.
    - Se o contador de tentativas atingir 3, o erro é registrado nos logs e o contador é resetado. A exceção `OrderProcessingException` é lançada para indicar falha no processamento do pedido.
    - Caso a validação seja bem-sucedida, o evento do pedido é salvo no banco de dados utilizando o `orderService.save`.

3. **Exceções e Tentativas de Reprocessamento**
    - Caso o evento de pedido falhe na validação, ele tenta reprocessar até 3 vezes. Se o número de tentativas atingir 3, os erros são registrados e o contador é resetado.
    - Caso o evento seja validado corretamente, ele é persistido.

4. **Métodos auxiliares**
    - **`validationOrderEventFields`**: Valida os campos do `OrderEvent` e retorna uma lista de mensagens de erro se houver violações.
    - **`convertMessageToOrder`**: Converte a mensagem do RabbitMQ para um objeto `OrderEvent` usando o `ObjectMapper`.

---

### **Resumo das Funções de Cada Consumer**
- **CustomerConsumer**:
    - Escuta a fila de clientes e valida os eventos de clientes.
    - Salva o evento no banco de dados se for válido.
    - Envia o evento para a DLQ se ocorrerem erros no processamento ou se a validação falhar.

- **OrderConsumer**:
    - Escuta a fila de pedidos e valida os eventos de pedidos.
    - Reprocessa os eventos até 3 vezes em caso de falha na validação.
    - Salva o evento no banco de dados se for válido.

---

## 7. **AddressEntity.java**, **CustomerEntity.java**, **TelephoneEntity.java**

Essas classes representam as **entidades de domínio** para o armazenamento de dados em um banco de dados **MongoDB**.

### AddressEntity.java:
- `@Data`: Lombok gera automaticamente os métodos **getters**, **setters**, **toString**, **equals**, e **hashCode**.
- `@AllArgsConstructor` e `@NoArgsConstructor`: Lombok gera o construtor com todos os campos e um construtor sem argumentos, respectivamente.
- Representa um endereço, com campos como **zipCode**, **street**, **number**, **complement**, **neighborhood** e **recipient**.

### CustomerEntity.java:
- `@Document(collection = "customers")`: Marca essa classe como uma entidade que será mapeada para a coleção `customers` no **MongoDB**.
- `@Id`: Indica que o campo `id` é a chave primária do MongoDB.
- `@Field(...)`: Marca campos que precisam ser mapeados para um nome específico no banco de dados.
- `fromCustomerEvent(CustomerEvent event)`: Método estático que converte um objeto de evento **CustomerEvent** (provavelmente proveniente de uma mensagem RabbitMQ) em um **CustomerEntity**, usando as informações de telefone e endereço. Isso transforma dados que são recebidos em um formato de mensagem para um formato persistente no banco de dados.

### TelephoneEntity.java:
- Representa os dados de um telefone, com campos como **ddd**, **number** e **type** (tipo de telefone, como "celular" ou "fixo").

---

## 8. **CustomerErrorEntity.java**

Representa um erro no processamento de um evento de cliente, com informações sobre o erro.

### Explicação do código:
- `@Document(collection = "customers-error")`: Essa classe será persistida na coleção `customers-error` no **MongoDB**.
- `customerEvent`: O evento do cliente que causou o erro.
- `errors`: Lista de mensagens de erro associadas ao evento.
- `toEntity(CustomerErrorEvent customerErrorEvent)`: Método que converte um **CustomerErrorEvent** (evento de erro) em um **CustomerErrorEntity** para persistência no MongoDB.

---

## 9. **OrderEntity.java**, **OrderItem.java**, **OrderErrorEntity.java**

Essas classes representam **entidades de domínio** para pedidos e itens de pedidos.

### OrderEntity.java:
- `@Document(collection = "orders")`: Mapeia essa classe para a coleção `orders` no **MongoDB**.
- `@MongoId`: Marca o campo `orderId` como a chave primária para o MongoDB (equivalente ao `@Id` no MongoDB).
- `@Indexed(name = "customer_id_index")`: Cria um índice no MongoDB para o campo `customerId` com o nome `customer_id_index`.
- `toEntity(OrderEvent orderEvent)`: Método estático que converte um **OrderEvent** (evento de pedido) em um **OrderEntity**, mapeando a lista de itens do pedido para objetos **OrderItem**.

### OrderItem.java:
- Representa um item do pedido, com campos como **product**, **quantity** e **price**.
- `@Field(targetType = FieldType.DECIMAL128)`: Mapeia o campo `price` como um tipo de dado **decimal** no MongoDB.

### OrderErrorEntity.java:
- Representa um erro no processamento de um evento de pedido.
- `orderEntity`: O pedido original que causou o erro.

---

## 10. **CustomerProcessingException.java**, **OrderProcessingException.java**

Essas são classes de **exceção personalizadas** para capturar erros durante o processamento de eventos de cliente e pedido, respectivamente.

### CustomerProcessingException.java:
- Extende **RuntimeException**.
- Contém um campo `value` para armazenar o CPF do cliente (o campo causador do erro).
- O método `getResult()` retorna o valor do **CPF** que causou o erro.

### OrderProcessingException.java:
- Similar à `CustomerProcessingException`, mas lida com eventos de pedido, contendo o **ID do pedido**.
- O método `getResult()` retorna o **ID do pedido** que causou o erro.

---

## 11. **CustomerSendDLQRetry.java**

Essa classe é responsável por **enviar mensagens para a Dead Letter Queue (DLQ)** quando o processamento falha após várias tentativas.

### Explicação do código:
- `sendToDLQ(CustomerErrorEvent customerErrorEvent, Integer retryCount)`: Envia o evento de erro do cliente para a fila **DLQ** `customer-queue-dlq`. A mensagem é convertida em **JSON** e o cabeçalho `x-dlq-retry` é adicionado para indicar o número de tentativas de reprocessamento da mensagem.
- `MessagePostProcessor getMessagePostProcessor(Integer retryCount)`: Cria um **MessagePostProcessor** que modifica os cabeçalhos da mensagem, especificamente adicionando o cabeçalho `x-dlq-retry`, que armazena o número de tentativas feitas para processar a mensagem.

---

## **Resumo**

- **Entidades de domínio**: São as classes que representam os dados persistidos no **MongoDB**. Elas incluem `CustomerEntity`, `AddressEntity`, `TelephoneEntity`, `OrderEntity` e `OrderItem`.
- **Exceções**: As classes `CustomerProcessingException` e `OrderProcessingException` capturam falhas no processamento de clientes e pedidos, respectivamente.
- **Publisher de DLQ**: A classe `CustomerSendDLQRetry` é responsável por enviar eventos de erro para a **Dead Letter Queue (DLQ)**, permitindo que as mensagens com falhas sejam tratadas ou analisadas posteriormente.

# Interfaces de Repositório e Serviços de Reprocessamento

As interfaces de repositório apresentadas utilizam o **Spring Data MongoDB** para a manipulação das entidades persistidas no banco de dados **MongoDB**. Elas extendem a interface **MongoRepository**, que fornece implementações automáticas para as operações CRUD (Criação, Leitura, Atualização e Exclusão) no MongoDB.

## Explicação de Cada Repositório

### 11. **CustomerErrorRepository.java**
- **MongoRepository<CustomerErrorEntity, ObjectId>**: Responsável pela manipulação das entidades **CustomerErrorEntity** no MongoDB.
    - **CustomerErrorEntity**: Representa os erros relacionados aos eventos de cliente.
    - **ObjectId**: Tipo de dado da chave primária no MongoDB, utilizado para **CustomerErrorEntity**.

Esse repositório pode ser utilizado para realizar operações como salvar, buscar, atualizar e excluir registros de erros relacionados a eventos de clientes.

### 12. **CustomerRepository.java**
- **MongoRepository<CustomerEntity, Long>**: Responsável pela manipulação das entidades **CustomerEntity** no MongoDB.
    - **CustomerEntity**: Representa um cliente.
    - **Long**: A chave primária para **CustomerEntity** é do tipo **Long** (geralmente o **customerId**).

Esse repositório facilita a execução de operações CRUD sobre os registros dos clientes.

### 13. **OrderErrorRepository.java**
- **MongoRepository<OrderErrorEntity, ObjectId>**: Responsável pela manipulação das entidades **OrderErrorEntity** no MongoDB.
    - **OrderErrorEntity**: Representa um erro ocorrido durante o processamento de um pedido.
    - **ObjectId**: A chave primária para **OrderErrorEntity** é do tipo **ObjectId**.

Esse repositório será útil para manipular registros de erros associados a pedidos.

### 14. **OrderRepository.java**
- **MongoRepository<OrderEntity, Long>**: Responsável pela manipulação das entidades **OrderEntity** no MongoDB.
    - **OrderEntity**: Representa um pedido.
    - **Long**: A chave primária para **OrderEntity** é do tipo **Long**, representando o **orderId**.

Esse repositório fornece operações CRUD para os registros de pedidos.

## Principais Características de MongoRepository

### 1. **Herança do MongoRepository**
- O **MongoRepository** fornece um conjunto de métodos prontos para serem usados para operações CRUD (como salvar, encontrar, deletar), sem a necessidade de escrever implementações para esses métodos.
- **Métodos comuns**:
    - `save(S entity)`: Salva um objeto no banco de dados.
    - `findById(ID id)`: Busca um objeto pelo seu ID.
    - `findAll()`: Retorna todos os registros da coleção.
    - `deleteById(ID id)`: Deleta um registro pelo seu ID.

### 2. **Consulta Personalizada**
- O **Spring Data MongoDB** permite criar consultas personalizadas através de **query methods**.
    - Exemplo: `findByCustomerId(Long customerId)`: Método para buscar um cliente específico a partir de seu **customerId**.

### 3. **Suporte para @Query (Consultas JPQL-like)**
- Utiliza a anotação **@Query** para criar consultas customizadas quando necessário.

### 4. **Indexação e Ordenação**
- A anotação **@Indexed** (como visto na entidade **OrderEntity**) pode ser combinada com os repositórios para otimizar consultas, criando índices no banco de dados. Esses índices ajudam a acelerar as operações de leitura para campos frequentemente consultados.

---

## Serviços de Reprocessamento

### 15. **BaseReprocessingDLQService**
A classe **BaseReprocessingDLQService** é uma classe abstrata que serve como base para reprocessamento de mensagens de uma **Dead Letter Queue (DLQ)** no RabbitMQ. Ela contém métodos e propriedades comuns a todos os serviços de reprocessamento, mas depende de implementações específicas para cada tipo de evento (ex: **CustomerErrorEvent** ou **OrderEvent**).

**Principais métodos**:
- `reprocessDlqMessages`: Recebe a mensagem da DLQ, converte em um evento específico, reenvia a mensagem para a fila principal ou salva o erro no banco de dados, dependendo do número de tentativas.
- Métodos abstratos:
    - `getDlqQueue()`: Retorna o nome da fila DLQ.
    - `getExchange()`: Retorna o nome do exchange.
    - `getRoutingKey()`: Retorna a routing key para enviar a mensagem de volta.
    - `getRetryHeader()`: Retorna o nome do cabeçalho que mantém o contador de tentativas.
    - `convertMessageToEvent(Message message)`: Converte a mensagem em um evento específico.
    - `saveErrorToDatabase(T event)`: Salva o erro no banco de dados.

---

### 16. **CustomerReprocessingService**
A classe **CustomerReprocessingService** estende **BaseReprocessingDLQService<CustomerErrorEvent>**, implementando métodos específicos para o reprocessamento de erros relacionados a eventos de cliente.

**Principais características**:
- Anotação `@Component`: Indica que a classe é gerenciada pelo Spring e pode ser injetada em outros componentes.
- Método `scheduleReprocessing`: Anotado com `@Scheduled(fixedDelay = 30000)`, executa o reprocessamento a cada 30 segundos.
- Implementação dos métodos abstratos:
    - `getDlqQueue()`: Retorna a fila DLQ para eventos de clientes.
    - `getExchange()`: Retorna o exchange para eventos de clientes.
    - `getRoutingKey()`: Retorna a routing key para eventos de clientes.
    - `getRetryHeader()`: Especifica o cabeçalho de retry para os eventos de clientes.
    - `convertMessageToEvent(Message message)`: Converte a mensagem da DLQ em um **CustomerErrorEvent**.
    - `saveErrorToDatabase(CustomerErrorEvent event)`: Salva o erro de evento no banco de dados.

---

### 17. **OrderReprocessingService**
A classe **OrderReprocessingService** segue a mesma estrutura que **CustomerReprocessingService**, mas é especializada no reprocessamento de eventos de pedido (do tipo **OrderEvent**).

**Características principais**:
- Anotação `@Component`: A classe é registrada como um componente Spring.
- Método `scheduleReprocessing`: Executado a cada 60 segundos, conforme `@Scheduled(fixedDelay = 60000)`.
- Implementação dos métodos abstratos:
    - `getDlqQueue()`: Retorna a fila DLQ para eventos de pedidos.
    - `getExchange()`: Retorna o exchange para eventos de pedidos.
    - `getRoutingKey()`: Retorna a routing key para eventos de pedidos.
    - `getRetryHeader()`: Especifica o cabeçalho de retry para os eventos de pedidos.
    - `convertMessageToEvent(Message message)`: Converte a mensagem da DLQ em um **OrderEvent**.
    - `saveErrorToDatabase(OrderEvent event)`: Salva o erro de evento no banco de dados.

---

### 18. **CustomerErrorService**
**CustomerErrorService** é um serviço responsável por salvar eventos de erro de cliente no banco de dados, utilizando o **CustomerErrorRepository**.

**Método**:
- `save(CustomerErrorEvent customerErrorEvent)`: Converte o evento de erro (**CustomerErrorEvent**) em uma entidade (**CustomerErrorEntity**) e a salva no banco de dados.

---

### 19. **OrderErrorService**
**OrderErrorService** é um serviço que salva eventos de erro de pedido no banco de dados, utilizando o **OrderErrorRepository**.

**Método**:
- `save(OrderEvent orderEvent)`: Converte o evento de erro (**OrderEvent**) em uma entidade (**OrderEntity**), cria uma entidade de erro (**OrderErrorEntity**) e a salva no banco de dados.

---

### 20. **CustomerService**
**CustomerService** é um serviço simples que lida com a persistência de dados de clientes, utilizando o **CustomerRepository**.

**Método**:
- `save(CustomerEvent event)`: Converte um evento de cliente (**CustomerEvent**) em uma entidade (**CustomerEntity**) e a salva no banco de dados.

---

### 21. **OrderService**
**OrderService** é um serviço responsável pela persistência de pedidos, utilizando o **OrderRepository**.

**Método**:
- `save(OrderEvent event)`: Converte o evento de pedido (**OrderEvent**) em uma entidade (**OrderEntity**), calcula o total do pedido e salva a entidade no banco de dados.

---

### Configuração do MongoDB

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://admin:123@localhost:27017/orderdb?authSource=admin
      authentication-database: admin
      auto-index-creation: true
      host: localhost
      port: 27017
      database: orderdb
      username: admin
      password: 123
```
- `spring.data.mongodb.uri`: A URI de conexão para o MongoDB, especificando o protocolo mongodb://, o nome de usuário admin, a senha 123, o host localhost, a porta 27017 e o nome do banco de dados orderdb. A parte authSource=admin indica que o MongoDB deve autenticar o usuário na base de dados admin.
- `spring.data.mongodb.authentication-database`: Especifica qual banco de dados o MongoDB deve usar para autenticar o usuário, que neste caso é admin.
- `spring.data.mongodb.auto-index-creation`: Definido como true, isso permite a criação automática de índices no MongoDB, o que pode melhorar a performance de consultas.
- `spring.data.mongodb.host`: Define o host onde o MongoDB está rodando, neste caso localhost.
- `spring.data.mongodb.port`: Especifica a porta do MongoDB, que é 27017 por padrão.
- `spring.data.mongodb.database`: O nome do banco de dados a ser utilizado, que é orderdb.

### Configuração do RabbitMQ

```yaml
spring:
  rabbit:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    concurrent-consumers: 3
    max-concurrent-consumers: 5
    prefetch-count: 10
    max-attempts: 3
```
- `spring.rabbit.host`: Define o host do RabbitMQ, que neste caso é localhost, ou seja, o RabbitMQ está rodando na mesma máquina da aplicação.
- `spring.rabbit.port`: Especifica a porta do RabbitMQ, que é 5672, a porta padrão do RabbitMQ.
- `spring.rabbit.username`: O nome de usuário para autenticação no RabbitMQ, que é guest (usuário padrão).
- `spring.rabbit.password`: A senha do usuário guest para o RabbitMQ.
- `spring.rabbit.virtual-host`: Define o virtual host a ser utilizado pelo RabbitMQ. O virtual host / é o padrão.
- `spring.rabbit.concurrent-consumers`: Define o número de consumidores concorrentes que podem consumir mensagens da fila ao mesmo tempo. No caso, são 3 consumidores.
- `spring.rabbit.max-concurrent-consumers`: Especifica o número máximo de consumidores concorrentes permitidos. Aqui, são 5 consumidores.
- `spring.rabbit.prefetch-count`: Define o número de mensagens que o consumidor pode "pegar" de uma vez antes de precisar enviar um ACK (confirmação de recebimento). Neste caso, cada consumidor pode pegar até 10 mensagens por vez.
- `spring.rabbit.max-attempts`: Especifica o número máximo de tentativas de processamento de uma mensagem. Neste caso, são 3 tentativas. Se o processamento falhar após 3 tentativas, a mensagem pode ser movida para a Dead Letter Queue (DLQ) ou descartada, dependendo da configuração.

## Considerações Finais
- **Reprocessamento de DLQ**: As classes **CustomerReprocessingService** e **OrderReprocessingService** gerenciam o reprocessamento de mensagens de erro, realizando tentativas de reenvio ou salvando os erros no banco de dados.
- **Gerenciamento de Erros**: Caso o reprocessamento falhe após várias tentativas, os eventos de erro são persistidos no banco de dados para análise posterior.
- **Integração com RabbitMQ**: O **RabbitTemplate** é utilizado para enviar mensagens ao RabbitMQ, seja para reprocessamento ou para reenvios de mensagens.
