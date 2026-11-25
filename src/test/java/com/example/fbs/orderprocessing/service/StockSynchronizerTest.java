package com.example.fbs.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.fbs.orderprocessing.config.DefaultMarketplaceRules;
import com.example.fbs.orderprocessing.dto.PushResult;
import com.example.fbs.orderprocessing.dto.SyncResult;
import com.example.fbs.orderprocessing.integration.ConnectorRegistry;
import com.example.fbs.orderprocessing.integration.MarketplaceConnector;
import com.example.fbs.orderprocessing.integration.StubMarketplaceConnector;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.MarketplaceOrder;
import com.example.fbs.orderprocessing.model.StockUpdate;

/** A35 «Синхронизировать остаток». */
class StockSynchronizerTest {

    private static List<StockUpdate> updates(int count) {
        return IntStream.range(0, count).mapToObj(i -> new StockUpdate("SKU-" + i, i)).toList();
    }

    @Test
    @DisplayName("Остатки уходят на все площадки пачками по правилам")
    void stocksArePushedToAllMarketplacesInBatches() {
        Fixture f = new Fixture();

        SyncResult r = f.synchronizer.sync(updates(250));

        assertEquals(3, f.ozon.pushedBatches().size()); // Ozon: по 100, значит 100 + 100 + 50
        assertEquals(1, f.wb.pushedBatches().size());   // WB: по 1000
        assertEquals(1, f.ym.pushedBatches().size());   // ЯМ: по 500
        assertEquals(5, r.batches());
        assertEquals(250, r.acceptedByMarketplace().get(Marketplace.OZON).intValue());
        assertFalse(r.hasErrors());
    }

    @Test
    @DisplayName("Пустой список ничего не отправляет")
    void emptyListSendsNothing() {
        Fixture f = new Fixture();

        SyncResult r = f.synchronizer.sync(List.of());

        assertEquals(0, r.batches());
        assertTrue(f.wb.pushedBatches().isEmpty());
    }

    @Test
    @DisplayName("Ошибка одной площадки не останавливает другие")
    void failureOfOneMarketplaceDoesNotStopOthers() {
        MarketplaceConnector broken = new MarketplaceConnector() {
            public Marketplace marketplace() { return Marketplace.YANDEX_MARKET; }
            public List<MarketplaceOrder> fetchNewOrders(Instant since) { return List.of(); }
            public PushResult pushStocks(List<StockUpdate> batch) {
                throw new IllegalStateException("401 Unauthorized");
            }
        };
        StubMarketplaceConnector wb = new StubMarketplaceConnector(Marketplace.WB);
        StockSynchronizer sync = new StockSynchronizer(
                new ConnectorRegistry(List.of(wb, broken)), new DefaultMarketplaceRules());

        SyncResult r = sync.sync(updates(3));

        assertEquals(3, r.acceptedByMarketplace().get(Marketplace.WB).intValue());
        assertEquals(1, r.errors().size());
        assertTrue(r.errors().get(0).contains("401"));
    }
}
