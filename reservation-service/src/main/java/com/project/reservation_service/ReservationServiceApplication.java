package com.project.reservation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.project.reservation_service.client")
@EnableDiscoveryClient
public class ReservationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}
