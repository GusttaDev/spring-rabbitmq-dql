
package com.gusttadev.orderconsumer.repository;

import com.gusttadev.orderconsumer.domain.entity.order.OrderErrorEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderErrorRepository extends MongoRepository<OrderErrorEntity, ObjectId> {
}
