package com.velsis.speedviolation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI speedViolationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Speed Violation Service API")
                .version("1.0.0")
                .description("Microsservico de apuracao de infracoes por excesso de velocidade "
                        + "conforme o Art. 218 do Codigo de Transito Brasileiro."));
    }
}
