package org.zigmoi.ketchup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableResourceServer
@EnableScheduling
public class KetchupApplication {

	public static void main(String[] args) {
		SpringApplication.run(KetchupApplication.class, args);
	}
}
