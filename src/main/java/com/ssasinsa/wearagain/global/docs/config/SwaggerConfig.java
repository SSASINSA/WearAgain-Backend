package com.ssasinsa.wearagain.global.docs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"})
public class SwaggerConfig {

    private static final String USER_JWT = "userJWT";
    private static final String ADMIN_JWT = "adminJWT";

    @Bean
    public OpenAPI openAPI() {
        Components components = new Components()
                .addSecuritySchemes(USER_JWT, bearerScheme("일반 사용자용 토큰"))
                .addSecuritySchemes(ADMIN_JWT, bearerScheme("관리자 전용 토큰"));

        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title("WearAgain API Docs")
                        .description("도메인별 @ApiDoc 기반 자동화된 Swagger 명세")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(USER_JWT))
                .addSecurityItem(new SecurityRequirement().addList(ADMIN_JWT));
    }

    private SecurityScheme bearerScheme(String description) {
        return new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(description);
    }
}
