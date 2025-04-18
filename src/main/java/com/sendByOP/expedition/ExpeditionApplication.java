package com.sendByOP.expedition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExpeditionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpeditionApplication.class, args);
	}

}
