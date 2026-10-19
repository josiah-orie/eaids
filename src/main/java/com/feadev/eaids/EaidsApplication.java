package com.feadev.eaids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class EaidsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EaidsApplication.class, args);
	}

}
