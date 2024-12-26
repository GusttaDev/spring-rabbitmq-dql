package com.gusttadev.orderconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OrderConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderConsumerApplication.class, args);
	}

}
