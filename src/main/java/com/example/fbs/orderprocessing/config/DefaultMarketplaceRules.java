package com.example.fbs.orderprocessing.config;

import org.springframework.stereotype.Component;
import com.example.fbs.orderprocessing.model.Marketplace;

/**
 * Заглушка правил площадок. Размеры пачек условные:
 * перед подключением реальных API сверить с документацией WB, Ozon и Яндекс Маркета.
 */
@Component
public class DefaultMarketplaceRules implements MarketplaceRules {

    @Override
    public int stockBatchSize(Marketplace marketplace) {
        return switch (marketplace) {
            case WB -> 1000;
            case OZON -> 100;
            case YANDEX_MARKET -> 500;
        };
    }
}
