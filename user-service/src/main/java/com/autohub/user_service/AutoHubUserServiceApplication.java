package com.autohub.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoHubUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoHubUserServiceApplication.class, args);
	}

}
