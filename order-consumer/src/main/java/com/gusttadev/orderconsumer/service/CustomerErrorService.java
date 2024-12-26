package com.gusttadev.orderconsumer.service;

import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerErrorEvent;
import com.gusttadev.orderconsumer.domain.entity.customer.CustomerErrorEntity;
import com.gusttadev.orderconsumer.repository.CustomerErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerErrorService {

    private final CustomerErrorRepository customerErrorRepository;

    public void save(CustomerErrorEvent customerErrorEvent) {
        CustomerErrorEntity entity = CustomerErrorEntity.toEntity(customerErrorEvent);
        customerErrorRepository.save(entity);
    }
}
