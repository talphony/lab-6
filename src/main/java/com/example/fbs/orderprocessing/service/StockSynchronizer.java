package com.example.fbs.orderprocessing.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.example.fbs.orderprocessing.config.MarketplaceRules;
import com.example.fbs.orderprocessing.dto.PushResult;
import com.example.fbs.orderprocessing.dto.SyncResult;
import com.example.fbs.orderprocessing.integration.ConnectorRegistry;
import com.example.fbs.orderprocessing.integration.MarketplaceConnector;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.StockUpdate;

/**
 * A35 «Синхронизировать остаток».
 * Вход: доступный остаток. Выход: остатки для маркетплейса.
 * Управление: правила маркетплейсов (размер пачки). Механизм: WMS.
 *
 * Ошибка одной пачки или одной площадки не останавливает остальные: она попадает в errors,
 * а повтор выполнит следующий прогон.
 */
@Service
public class StockSynchronizer {

    private final ConnectorRegistry connectors;
    private final MarketplaceRules rules;

    public StockSynchronizer(ConnectorRegistry connectors, MarketplaceRules rules) {
        this.connectors = connectors;
        this.rules = rules;
    }

    public SyncResult sync(List<StockUpdate> updates) {
        if (updates.isEmpty()) {
            return SyncResult.empty();
        }
        Map<Marketplace, Integer> accepted = new EnumMap<>(Marketplace.class);
        List<String> errors = new ArrayList<>();
        int batches = 0;

        for (MarketplaceConnector connector : connectors.all()) {
            Marketplace m = connector.marketplace();
            for (List<StockUpdate> batch : split(updates, rules.stockBatchSize(m))) {
                batches++;
                try {
                    PushResult result = connector.pushStocks(batch);
                    accepted.merge(m, result.accepted(), Integer::sum);
                    result.errors().forEach(e -> errors.add(m + ": " + e));
                } catch (RuntimeException ex) {
                    errors.add(m + ": пачка из " + batch.size() + " позиций не отправлена: " + ex.getMessage());
                }
            }
        }
        return new SyncResult(accepted, batches, errors);
    }

    static <T> List<List<T>> split(List<T> items, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер пачки должен быть больше 0");
        }
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            result.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return result;
    }
}
