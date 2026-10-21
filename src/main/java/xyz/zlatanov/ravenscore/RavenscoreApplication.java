package xyz.zlatanov.ravenscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RavenscoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(RavenscoreApplication.class, args);
	}

}
