package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication(
		scanBasePackages = "org.example",
		exclude = {DataSourceAutoConfiguration.class}
)
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class StudioRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudioRestApplication.class, args);
	}
}
