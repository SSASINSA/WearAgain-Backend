package com.ssasinsa.wearagain;

import com.ssasinsa.wearagain.domain.auth.config.AppleOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.config.AuthRedisProperties;
import com.ssasinsa.wearagain.domain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.config.KakaoOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.config.JwtProperties;
import com.ssasinsa.wearagain.domain.auth.config.AdminJwtProperties;
import com.ssasinsa.wearagain.domain.auth.config.AdminSuperAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableConfigurationProperties({
        JwtProperties.class,
        AuthRedisProperties.class,
        GoogleOAuthProperties.class,
        KakaoOAuthProperties.class,
        AppleOAuthProperties.class,
        AdminJwtProperties.class,
        AdminSuperAdminProperties.class
})
@SpringBootApplication
public class WearagainApplication {

	public static void main(String[] args) {
		SpringApplication.run(WearagainApplication.class, args);
	}

}
