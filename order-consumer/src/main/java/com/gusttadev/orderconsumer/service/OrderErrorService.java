package com.gusttadev.orderconsumer.service;

import com.gusttadev.orderconsumer.consumer.dto.order.OrderEvent;
import com.gusttadev.orderconsumer.domain.entity.order.OrderErrorEntity;
import com.gusttadev.orderconsumer.domain.entity.order.OrderEntity;
import com.gusttadev.orderconsumer.repository.OrderErrorRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderErrorService {

    private final OrderErrorRepository orderErrorRepository;

    public OrderErrorService(OrderErrorRepository orderErrorRepository) {
        this.orderErrorRepository = orderErrorRepository;
    }

    public void save(OrderEvent orderEvent) {
        OrderEntity orderEntity = OrderEntity.toEntity(orderEvent);
        OrderErrorEntity orderDLQEntity = new OrderErrorEntity(orderEntity);
        orderErrorRepository.save(orderDLQEntity);
    }
}
