package com.ffresco.pricelist.infrastructure.config;

import com.ffresco.pricelist.application.port.out.LoadProductsPort;
import com.ffresco.pricelist.infrastructure.adapter.out.memory.InMemoryProductAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {

    @Bean
    public LoadProductsPort loadProductsPort() {
        return new InMemoryProductAdapter();
    }
}
