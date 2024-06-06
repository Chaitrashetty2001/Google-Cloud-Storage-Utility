package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "GCS-Utility", description = "This project consist of below API's which has functionalities mentioned below. "))
public class GcsUtilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(GcsUtilityApplication.class, args);
	}

}
