package com.teamtacles.teamtacles_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "TeamTacles API", version = "1.0", description = "TeamTacles API Documentation – Team Task Management"))
@SpringBootApplication
public class TeamtaclesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamtaclesApiApplication.class, args);
	}

}
