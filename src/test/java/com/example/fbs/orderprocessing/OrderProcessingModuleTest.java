package com.example.fbs.orderprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.fbs.orderprocessing.demo.DemoService;
import com.example.fbs.orderprocessing.demo.DemoState;
import com.example.fbs.orderprocessing.dto.ProcessingReport;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.model.StockUpdate;
import com.example.fbs.orderprocessing.service.OrderProcessingService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Проверка модуля A3 целиком в Spring-контексте: все бины связаны так же, как при запуске приложения.
 * Сценарий — демо-данные из DemoService.seed().
 */
@SpringBootTest(properties = {
        "wms.demo.enabled=true",
        "wms.safety-buffer=1",
        "wms.order-processing.scheduler-enabled=false"
})
class OrderProcessingModuleTest {

    @Autowired
    private DemoService demo;

    @Autowired
    private OrderProcessingService processing;

    @BeforeEach
    void seed() {
        demo.seed();
    }

    private OrderStatus status(String key) {
        return demo.state().orders().stream()
                .filter(o -> o.key().equals(key))
                .findFirst().orElseThrow().status();
    }

    @Test
    @DisplayName("Прогон A3 на демо-данных: 3 резерва, 1 нехватка, остатки выгружены на все площадки")
    void fullScenario() {
        ProcessingReport report = processing.runSinceLastRun();

        assertEquals(4, report.loaded());
        assertEquals(3, report.reserved());
        assertEquals(1, report.notEnoughStock());
        assertEquals(OrderStatus.NOT_ENOUGH_STOCK, status("OZON:OZ-2001"));
        assertEquals(OrderStatus.RESERVED, status("WB:WB-1001"));

        // Футболка: 10 на складе, 3 (WB) + 1 (ЯМ) в резерве, буфер 1, доступно 5.
        assertTrue(report.stockUpdates().contains(new StockUpdate("TS-OVR-BLK-L", 5)));

        DemoState state = demo.state();
        assertEquals(3, state.assemblyTasks().size());
        for (Marketplace m : Marketplace.values()) {
            assertEquals(1, state.pushedStocks().get(m).size(), "одна пачка на " + m);
        }
        assertTrue(report.syncResult().errors().isEmpty());
    }

    @Test
    @DisplayName("Отмена заказа возвращает товар в доступный остаток")
    void cancellationReturnsStock() {
        processing.runSinceLastRun();
        demo.cancelOrder(Marketplace.WB, "WB-1001");

        ProcessingReport report = processing.runSinceLastRun();

        assertEquals(1, report.cancelled());
        assertEquals(OrderStatus.CANCELLED, status("WB:WB-1001"));
        // Футболка: 10 − 1 (ЯМ) − буфер 1 = 8.
        assertEquals(List.of(new StockUpdate("TS-OVR-BLK-L", 8)), report.stockUpdates());
    }
}
