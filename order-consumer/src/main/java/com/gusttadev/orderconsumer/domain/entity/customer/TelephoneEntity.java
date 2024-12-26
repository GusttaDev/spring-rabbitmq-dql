package com.gusttadev.orderconsumer.domain.entity.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelephoneEntity {

    private int ddd;
    private String number;
    private String type;
}