package com.gusttadev.orderconsumer.repository;

import com.gusttadev.orderconsumer.domain.entity.order.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderEntity, Long> {
}
