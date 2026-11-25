package com.example.fbs.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.example.fbs.orderprocessing.service.Fixture.SINCE;
import static com.example.fbs.orderprocessing.service.Fixture.cancel;
import static com.example.fbs.orderprocessing.service.Fixture.line;
import static com.example.fbs.orderprocessing.service.Fixture.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.fbs.orderprocessing.dto.LoadResult;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.OrderStatus;

/** A31 «Загрузить заказы». */
class OrderLoaderTest {

    private final Fixture f = new Fixture();

    @Test
    @DisplayName("Новый заказ сохраняется в статусе NEW")
    void newOrderIsSavedAsNew() {
        f.wb.enqueue(order(Marketplace.WB, "1001", line("TS-L", 1)));

        LoadResult result = f.loader.load(Marketplace.WB, SINCE);

        assertEquals(1, result.newOrders().size());
        assertEquals(OrderStatus.NEW, f.orders.findByKey("WB:1001").orElseThrow().status());
    }

    @Test
    @DisplayName("Повторно пришедший заказ не дублируется")
    void repeatedOrderIsNotDuplicated() {
        f.wb.enqueue(order(Marketplace.WB, "1001", line("TS-L", 1)));
        f.loader.load(Marketplace.WB, SINCE);
        f.wb.enqueue(order(Marketplace.WB, "1001", line("TS-L", 1)));
        f.wb.enqueue(order(Marketplace.WB, "1002", line("TS-L", 1)));
        f.wb.enqueue(order(Marketplace.WB, "1002", line("TS-L", 1)));

        LoadResult result = f.loader.load(Marketplace.WB, SINCE);

        assertEquals(1, result.newOrders().size());
        assertEquals("1002", result.newOrders().get(0).externalId());
    }

    @Test
    @DisplayName("Одинаковый номер на разных площадках — разные заказы")
    void sameNumberOnDifferentMarketplacesAreDifferentOrders() {
        f.wb.enqueue(order(Marketplace.WB, "555", line("TS-L", 1)));
        f.ozon.enqueue(order(Marketplace.OZON, "555", line("TS-L", 1)));

        assertEquals(1, f.loader.load(Marketplace.WB, SINCE).newOrders().size());
        assertEquals(1, f.loader.load(Marketplace.OZON, SINCE).newOrders().size());
    }

    @Test
    @DisplayName("Отмена известного заказа попадает в отмены")
    void cancellationOfKnownOrderIsReturned() {
        f.wb.enqueue(order(Marketplace.WB, "1001", line("TS-L", 1)));
        f.loader.load(Marketplace.WB, SINCE);
        f.wb.enqueue(cancel(Marketplace.WB, "1001", line("TS-L", 1)));

        LoadResult result = f.loader.load(Marketplace.WB, SINCE);

        assertTrue(result.newOrders().isEmpty());
        assertEquals(1, result.cancelledOrders().size());
    }

    @Test
    @DisplayName("Отмена неизвестного заказа игнорируется")
    void cancellationOfUnknownOrderIsIgnored() {
        f.wb.enqueue(cancel(Marketplace.WB, "9999", line("TS-L", 1)));

        LoadResult result = f.loader.load(Marketplace.WB, SINCE);

        assertTrue(result.newOrders().isEmpty());
        assertTrue(result.cancelledOrders().isEmpty());
        assertTrue(f.orders.findByKey("WB:9999").isEmpty());
    }
}
