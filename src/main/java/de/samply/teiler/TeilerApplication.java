package de.samply.teiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"de.samply"})
@EntityScan(basePackages = {"de.samply.db.model"})
@EnableJpaRepositories("de.samply.db.repository")
@EnableScheduling
public class TeilerApplication {

  public static void main(String[] args) {
    SpringApplication.run(TeilerApplication.class, args);
  }

}
