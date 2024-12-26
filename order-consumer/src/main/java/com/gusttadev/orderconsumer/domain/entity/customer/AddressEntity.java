package com.gusttadev.orderconsumer.domain.entity.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressEntity {

    private String zipCode;
    private int number;
    private String complement;
    private String street;
    private String neighborhood;
    private String recipient;
}