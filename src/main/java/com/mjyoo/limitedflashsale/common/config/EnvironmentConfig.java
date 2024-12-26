package com.mjyoo.limitedflashsale.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Configuration
public class EnvironmentConfig {
    private final Dotenv dotenv;

    public EnvironmentConfig(){
        String envFile = Files.exists(Paths.get("prod.env")) ? "prod.env" : ".env";
        this.dotenv = Dotenv.configure().filename(envFile).load();
    }

    public String getJwtSecretKey() {
        return dotenv.get("JWT_SECRET_KEY");
    }

    public String getAdminToken() {
        return dotenv.get("ADMIN_TOKEN");
    }

    @PostConstruct
    public void loadEnvironmentVariables() {
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("SPRING_MAIL_USERNAME", dotenv.get("SPRING_MAIL_USERNAME"));
        System.setProperty("SPRING_MAIL_PASSWORD", dotenv.get("SPRING_MAIL_PASSWORD"));
    }

}
