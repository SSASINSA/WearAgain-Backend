package com.ssasinsa.wearagain;

import com.ssasinsa.wearagain.auth.config.AuthRedisProperties;
import com.ssasinsa.wearagain.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableConfigurationProperties({JwtProperties.class, AuthRedisProperties.class})
@SpringBootApplication
public class WearagainApplication {

	public static void main(String[] args) {
		SpringApplication.run(WearagainApplication.class, args);
	}

}
