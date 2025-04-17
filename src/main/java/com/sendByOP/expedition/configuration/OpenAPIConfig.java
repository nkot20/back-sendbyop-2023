package com.sendByOP.expedition.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OpenAPIConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
            .url("/api/v1")
            .description("Local server");

        return new OpenAPI()
                .info(new Info()
                        .title("SendByOP API")
                        .version("1.0")
                        .description("API documentation")
                        .license(new License().name("Apache 2.0")))
                .servers(List.of(localServer));
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .defaultContentType(MediaType.APPLICATION_JSON)
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("*/*", MediaType.APPLICATION_JSON);
    }
}