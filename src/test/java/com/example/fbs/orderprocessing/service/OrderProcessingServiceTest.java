package com.example.fbs.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.example.fbs.orderprocessing.service.Fixture.SINCE;
import static com.example.fbs.orderprocessing.service.Fixture.cancel;
import static com.example.fbs.orderprocessing.service.Fixture.line;
import static com.example.fbs.orderprocessing.service.Fixture.order;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.fbs.orderprocessing.dto.ProcessingReport;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.model.StockUpdate;

/** A3 «Обработать заказ» целиком: A31, затем A32 и A33, затем A34 и A35. */
class OrderProcessingServiceTest {

    @Test
    @DisplayName("Полный прогон: резерв, пересчёт и выгрузка остатков")
    void fullRunReservesAndPushesStocks() {
        Fixture f = new Fixture(1);
        f.stock.put("TS-L", 10);
        f.stock.put("MUG", 2);
        f.wb.enqueue(order(Marketplace.WB, "1", line("TS-L", 3)));
        f.ozon.enqueue(order(Marketplace.OZON, "2", line("MUG", 5))); // не хватит

        ProcessingReport report = f.processing.process(SINCE);

        assertEquals(2, report.loaded());
        assertEquals(1, report.reserved());
        assertEquals(1, report.notEnoughStock());
        assertEquals(List.of(new StockUpdate("TS-L", 6)), report.stockUpdates()); // 10 − 3 − 1
        assertEquals(List.of(new StockUpdate("TS-L", 6)), f.wb.pushedBatches().get(0));
        assertEquals(1, f.assemblyTasks.published().size());
        assertEquals(OrderStatus.NOT_ENOUGH_STOCK, f.orders.findByKey("OZON:2").orElseThrow().status());
    }

    @Test
    @DisplayName("Отмена освобождает товар для нового заказа в том же прогоне")
    void cancellationFreesStockForNewOrderInSameRun() {
        Fixture f = new Fixture();
        f.stock.put("TS-L", 2);
        f.wb.enqueue(order(Marketplace.WB, "1", line("TS-L", 2)));
        f.processing.process(SINCE);

        f.wb.enqueue(cancel(Marketplace.WB, "1", line("TS-L", 2)));
        f.wb.enqueue(order(Marketplace.WB, "2", line("TS-L", 2)));
        ProcessingReport report = f.processing.process(SINCE);

        assertEquals(1, report.cancelled());
        assertEquals(1, report.reserved());
        assertEquals(OrderStatus.RESERVED, f.orders.findByKey("WB:2").orElseThrow().status());
        assertEquals(List.of(new StockUpdate("TS-L", 0)), report.stockUpdates());
    }

    @Test
    @DisplayName("Без новых заказов остатки не выгружаются")
    void noNewOrdersNoStockPush() {
        Fixture f = new Fixture();

        ProcessingReport report = f.processing.process(SINCE);

        assertEquals(0, report.loaded());
        assertTrue(report.stockUpdates().isEmpty());
        assertTrue(f.wb.pushedBatches().isEmpty());
    }

    @Test
    @DisplayName("Ручной запуск сохраняет последний отчёт")
    void runSinceLastRunStoresLastReport() {
        Fixture f = new Fixture();
        f.stock.put("TS-L", 1);
        f.wb.enqueue(order(Marketplace.WB, "1", line("TS-L", 1)));

        ProcessingReport report = f.processing.runSinceLastRun();

        assertEquals(1, report.reserved());
        assertEquals(report, f.processing.lastReport().orElseThrow());
    }
}
