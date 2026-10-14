package com.shushinestudio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Shushine Studio Web API",
                description = "API RESTful oficial para la gestión integral y reservas del salón de belleza Shushine Studio.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Alex Fernando Alfaro Diaz",
                        email = "lalafaro6@gmail.com"
                )
        ),
        security = @SecurityRequirement(name = "Bearer Token")
)
@SecurityScheme(
        name = "Bearer Token",
        description = "Autenticación mediante JWT Bearer token. Inicia sesión en /api/auth/login para obtener el token.",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
