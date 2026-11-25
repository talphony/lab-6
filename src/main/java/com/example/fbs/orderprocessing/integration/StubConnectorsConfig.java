package com.example.fbs.orderprocessing.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.fbs.orderprocessing.model.Marketplace;

/** Регистрирует по заглушке коннектора на каждую площадку. */
@Configuration
public class StubConnectorsConfig {

    @Bean
    public StubMarketplaceConnector wbConnector() {
        return new StubMarketplaceConnector(Marketplace.WB);
    }

    @Bean
    public StubMarketplaceConnector ozonConnector() {
        return new StubMarketplaceConnector(Marketplace.OZON);
    }

    @Bean
    public StubMarketplaceConnector yandexMarketConnector() {
        return new StubMarketplaceConnector(Marketplace.YANDEX_MARKET);
    }
}
