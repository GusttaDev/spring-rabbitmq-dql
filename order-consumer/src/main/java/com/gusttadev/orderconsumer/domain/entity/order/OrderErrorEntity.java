package com.gusttadev.orderconsumer.domain.entity.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders-errors")
public class OrderErrorEntity {

    @Id
    private ObjectId id;
    private OrderEntity orderEntity;

    public OrderErrorEntity(OrderEntity orderEntity) {
        this.orderEntity = orderEntity;
    }
}
