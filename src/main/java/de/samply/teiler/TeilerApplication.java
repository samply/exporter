package de.samply.teiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"de.samply"})
public class TeilerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeilerApplication.class, args);
	}

}