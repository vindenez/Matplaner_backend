package com.example.tasterj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TasterjApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasterjApplication.class, args);
	}

}
