package com.gusttadev.orderconsumer.domain.entity.order;

import com.gusttadev.orderconsumer.consumer.dto.order.OrderEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class OrderEntity {

    @MongoId
    private Long orderId;

    @Indexed(name = "customer_id_index")
    private Long customerId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal total;

    private List<OrderItem> items;
    public static OrderEntity toEntity(OrderEvent orderEvent){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(orderEvent.orderId());
        orderEntity.setCustomerId(orderEvent.customerId());
        orderEntity.setItems(getOrderItems(orderEvent));

        return orderEntity;
    }

    private static List<OrderItem> getOrderItems(OrderEvent event) {
        return event.items().stream()
                .map(i -> new OrderItem(i.product(), i.quantity(), i.price()))
                .toList();
    }
}