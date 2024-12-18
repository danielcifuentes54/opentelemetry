package com.dc.opentelemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class OpentelemetryApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpentelemetryApplication.class, args);
	}

}
