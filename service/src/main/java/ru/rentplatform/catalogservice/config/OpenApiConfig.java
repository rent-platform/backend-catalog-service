package ru.rentplatform.catalogservice.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogServiceOpenAPI(
            @Value("${app.swagger.gateway-url}") String gatewayUrl,
            @Value("${app.swagger.catalog-service-url}") String catalogServiceUrl
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Catalog Service API")
                        .description("API для каталога аренды Rent Platform")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url(gatewayUrl).description("Gateway"),
                        new Server().url(catalogServiceUrl).description("Catalog Service (Direct)")
                ));
    }
}
