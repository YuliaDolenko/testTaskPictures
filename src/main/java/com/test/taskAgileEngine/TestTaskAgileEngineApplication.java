package com.test.taskAgileEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TestTaskAgileEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestTaskAgileEngineApplication.class, args);
	}

}
