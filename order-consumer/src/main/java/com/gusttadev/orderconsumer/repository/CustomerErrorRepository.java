
package com.gusttadev.orderconsumer.repository;

import com.gusttadev.orderconsumer.domain.entity.customer.CustomerErrorEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerErrorRepository extends MongoRepository<CustomerErrorEntity, ObjectId> {
}
