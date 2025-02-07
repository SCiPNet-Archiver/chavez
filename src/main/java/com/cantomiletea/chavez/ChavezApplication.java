package com.cantomiletea.chavez;

import com.cantomiletea.chavez.auth.RSAKeyRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RSAKeyRecord.class)
public class ChavezApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChavezApplication.class, args);
	}

}
