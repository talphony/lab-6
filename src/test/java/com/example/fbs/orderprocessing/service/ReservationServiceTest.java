package com.example.fbs.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.example.fbs.orderprocessing.service.Fixture.line;
import static com.example.fbs.orderprocessing.service.Fixture.order;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.fbs.orderprocessing.dto.ReservationResult;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderLine;
import com.example.fbs.orderprocessing.model.OrderStatus;

/** A33 «Зарезервировать товар». */
class ReservationServiceTest {

    private final Fixture f = new Fixture();

    private Order newOrder(String id, OrderLine... lines) {
        Order o = Order.from(order(Marketplace.WB, id, lines));
        f.orders.save(o);
        return o;
    }

    @Test
    @DisplayName("Хватает остатка: ставится резерв и выдаётся задание на сборку")
    void enoughStockCreatesReservationAndAssemblyTask() {
        f.stock.put("TS-L", 5);
        Order o = newOrder("1", line("TS-L", 2));

        ReservationResult r = f.reservationService.reserve(o);

        assertTrue(r.reserved());
        assertEquals(OrderStatus.RESERVED, o.status());
        assertEquals(2, f.reservations.reservedQuantity("TS-L"));
        assertEquals(1, f.assemblyTasks.published().size());
        assertEquals("WB:1", f.assemblyTasks.published().get(0).orderKey());
    }

    @Test
    @DisplayName("Не хватает одного товара: ничего не резервируется")
    void shortageOfOneSkuReservesNothing() {
        f.stock.put("TS-L", 5);
        f.stock.put("MUG", 1);
        Order o = newOrder("1", line("TS-L", 2), line("MUG", 3));

        ReservationResult r = f.reservationService.reserve(o);

        assertFalse(r.reserved());
        assertEquals(List.of("MUG"), r.shortageSkus());
        assertEquals(OrderStatus.NOT_ENOUGH_STOCK, o.status());
        assertEquals(0, f.reservations.reservedQuantity("TS-L"));
        assertTrue(f.assemblyTasks.published().isEmpty());
    }

    @Test
    @DisplayName("Учитывается резерв других заказов")
    void reservationsOfOtherOrdersAreTakenIntoAccount() {
        f.stock.put("TS-L", 3);
        f.reservationService.reserve(newOrder("1", line("TS-L", 2)));

        ReservationResult second = f.reservationService.reserve(newOrder("2", line("TS-L", 2)));

        assertFalse(second.reserved());
    }

    @Test
    @DisplayName("Повторяющиеся строки одного товара суммируются")
    void repeatedLinesOfSameSkuAreSummed() {
        f.stock.put("TS-L", 3);

        ReservationResult r = f.reservationService.reserve(newOrder("1", line("TS-L", 2), line("TS-L", 2)));

        assertFalse(r.reserved());
    }

    @Test
    @DisplayName("Страховой буфер не мешает резерву уже проданного заказа")
    void safetyBufferDoesNotBlockSoldOrder() {
        Fixture withBuffer = new Fixture(5);
        withBuffer.stock.put("TS-L", 2);
        Order o = Order.from(order(Marketplace.WB, "1", line("TS-L", 2)));
        withBuffer.orders.save(o);

        assertTrue(withBuffer.reservationService.reserve(o).reserved());
    }

    @Test
    @DisplayName("Повторный резерв запрещён")
    void repeatedReservationIsForbidden() {
        f.stock.put("TS-L", 5);
        Order o = newOrder("1", line("TS-L", 1));
        f.reservationService.reserve(o);

        assertThrows(IllegalStateException.class, () -> f.reservationService.reserve(o));
    }
}
