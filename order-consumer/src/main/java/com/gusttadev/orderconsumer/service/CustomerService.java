package com.gusttadev.orderconsumer.service;

import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerEvent;
import com.gusttadev.orderconsumer.domain.entity.customer.CustomerEntity;
import com.gusttadev.orderconsumer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public void save(CustomerEvent event) {
        CustomerEntity customerEntity = CustomerEntity.fromCustomerEvent(event);
        customerRepository.save(customerEntity);
    }
}
