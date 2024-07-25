package com.example;

import org.springframework.boot.SpringApplication;

public class TestDemoScyllaAlternatorApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoScyllaAlternatorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
