package com.gusttadev.orderconsumer.service;

import com.gusttadev.orderconsumer.consumer.dto.order.OrderEvent;
import com.gusttadev.orderconsumer.domain.entity.order.OrderEntity;
import com.gusttadev.orderconsumer.domain.entity.order.OrderItem;
import com.gusttadev.orderconsumer.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    public void save(OrderEvent event) {

        var entity = new OrderEntity();

        entity.setOrderId(event.orderId());
        entity.setCustomerId(event.customerId());
        entity.setItems(getOrderItems(event));
        entity.setTotal(getTotal(event));

        orderRepository.save(entity);
    }

    private BigDecimal getTotal(OrderEvent event) {
        return event.items()
                .stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static List<OrderItem> getOrderItems(OrderEvent event) {
        return event.items().stream()
                .map(i -> new OrderItem(i.product(), i.quantity(), i.price()))
                .toList();
    }
}
