package com.xuelei.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SAMLMessageHandlerConfiig {
    
    @Bean
    public SAMLMessageHandler getSAMLMessageHandler() {
        SAMLMessageHandler samlMessageHandler = new SAMLMessageHandler();
        return samlMessageHandler;
    }
    
}
