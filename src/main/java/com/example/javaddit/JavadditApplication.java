package com.example.javaddit;

import com.example.javaddit.core.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class JavadditApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavadditApplication.class, args);
    }
}
