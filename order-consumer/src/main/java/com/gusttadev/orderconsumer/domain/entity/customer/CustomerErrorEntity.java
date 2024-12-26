package com.gusttadev.orderconsumer.domain.entity.customer;

import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerErrorEvent;
import com.gusttadev.orderconsumer.consumer.dto.customer.CustomerEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers-error")
public class CustomerErrorEntity {
    @Id
    private ObjectId id;
    @Field("customer")
    private CustomerEvent customerEvent;
    private List<String> errors;

    public static CustomerErrorEntity toEntity(CustomerErrorEvent customerErrorEvent) {
        return CustomerErrorEntity.builder()
                .customerEvent(customerErrorEvent.customerEvent())
                .errors(customerErrorEvent.errors())
                .build();
    }
}
