package com.gusttadev.orderconsumer.domain.entity.customer;

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
@Document(collection = "customers")
public class CustomerEntity {

    @Id
    private ObjectId id;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("cpf")
    private String cpf;

    @Field("gender")
    private String gender;

    @Field("date_of_birth")
    private String dateOfBirth;

    @Field("phones")
    private List<TelephoneEntity> phones;

    @Field("addresses")
    private List<AddressEntity> addresses;

    public static CustomerEntity fromCustomerEvent(CustomerEvent event) {
        List<TelephoneEntity> phoneEntities = event.phones().stream()
            .map(phone -> new TelephoneEntity(phone.ddd(), phone.number(), phone.type()))
            .toList();

        List<AddressEntity> addressEntities = event.addresses().stream()
            .map(address -> new AddressEntity(address.zipCode(), address.number(), address.complement(),
                    address.street(), address.neighborhood(), address.recipient()))
            .toList();

        return CustomerEntity.builder()
                .firstName(event.firstName())
                .lastName(event.lastName())
                .cpf(event.cpf())
                .gender(event.gender())
                .dateOfBirth(String.valueOf(event.dateOfBirth()))
                .phones(phoneEntities)
                .addresses(addressEntities)
                .build();
    }
}
