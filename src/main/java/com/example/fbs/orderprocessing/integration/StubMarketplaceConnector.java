package com.example.fbs.orderprocessing.integration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.example.fbs.orderprocessing.dto.PushResult;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.MarketplaceOrder;
import com.example.fbs.orderprocessing.model.StockUpdate;

/**
 * Заглушка коннектора: в сеть не ходит.
 * Заказы, положенные через enqueue(), отдаёт один раз; выгруженные пачки запоминает.
 */
public class StubMarketplaceConnector implements MarketplaceConnector {

    private final Marketplace marketplace;
    private final List<MarketplaceOrder> queue = new ArrayList<>();
    private final List<List<StockUpdate>> pushedBatches = new ArrayList<>();

    public StubMarketplaceConnector(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    @Override
    public Marketplace marketplace() {
        return marketplace;
    }

    @Override
    public synchronized List<MarketplaceOrder> fetchNewOrders(Instant since) {
        List<MarketplaceOrder> result = List.copyOf(queue);
        queue.clear();
        return result;
    }

    @Override
    public synchronized PushResult pushStocks(List<StockUpdate> batch) {
        pushedBatches.add(List.copyOf(batch));
        return PushResult.ok(batch.size());
    }

    public synchronized void enqueue(MarketplaceOrder order) {
        queue.add(order);
    }

    public synchronized void clear() {
        queue.clear();
        pushedBatches.clear();
    }

    public synchronized List<List<StockUpdate>> pushedBatches() {
        return List.copyOf(pushedBatches);
    }
}
