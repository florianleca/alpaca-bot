package fr.flolec.alpacabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlpacaBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlpacaBotApplication.class, args);
	}

}
