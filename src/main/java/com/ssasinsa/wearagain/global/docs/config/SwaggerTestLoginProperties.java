package com.ssasinsa.wearagain.global.docs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swagger.test-login")
public class SwaggerTestLoginProperties {

    private final Admin admin = new Admin();

    public Admin getAdmin() {
        return admin;
    }

    public static class Admin {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
