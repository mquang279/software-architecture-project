package com.project.movie_reservation_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.project.movie_reservation_system.client")
public class MovieReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieReservationSystemApplication.class, args);
	}

}
