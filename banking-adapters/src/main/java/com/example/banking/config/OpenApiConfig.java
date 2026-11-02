package com.example.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingTransactionProcessorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Transaction Processor API")
                        .description("REST APIs for customer onboarding, account management, deposits, withdrawals, transfers, and OTP authorization.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Banking Application Team")));
    }
}
