package com.mertalptekin.springoauthserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/.well-known/openid-configuration")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET","POST")
                .allowCredentials(true);
    }
}
