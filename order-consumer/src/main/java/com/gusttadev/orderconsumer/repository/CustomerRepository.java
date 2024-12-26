package com.gusttadev.orderconsumer.repository;

import com.gusttadev.orderconsumer.domain.entity.customer.CustomerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<CustomerEntity, Long> {
}
