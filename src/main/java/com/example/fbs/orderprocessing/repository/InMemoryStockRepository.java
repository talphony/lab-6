package com.example.fbs.orderprocessing.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Заглушка остатков: хранит в памяти, заполняется через put(). */
@Component
public class InMemoryStockRepository implements StockRepository {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    @Override
    public int onHand(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    /** Снимок остатков для демо-эндпоинта. */
    public Map<String, Integer> snapshot() {
        return Map.copyOf(stock);
    }

    public void clear() {
        stock.clear();
    }

    public void put(String sku, int quantity) {
        stock.put(sku, quantity);
    }
}
