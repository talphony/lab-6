package com.example.fbs.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.example.fbs.orderprocessing.service.Fixture.line;
import static com.example.fbs.orderprocessing.service.Fixture.order;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.model.Reservation;

/** A32 «Обработать отмену». */
class CancellationHandlerTest {

    private final Fixture f = new Fixture();

    private Order reservedOrder() {
        f.stock.put("TS-L", 10);
        f.stock.put("MUG", 5);
        Order o = Order.from(order(Marketplace.OZON, "77", line("TS-L", 2), line("MUG", 1)));
        f.orders.save(o);
        f.reservationService.reserve(o);
        return o;
    }

    @Test
    @DisplayName("Отмена снимает весь резерв заказа")
    void cancellationReleasesAllReservations() {
        Order o = reservedOrder();

        List<Reservation> released = f.cancellationHandler.handle(o);

        assertEquals(2, released.size());
        assertEquals(0, f.reservations.reservedQuantity("TS-L"));
        assertEquals(0, f.reservations.reservedQuantity("MUG"));
        assertEquals(OrderStatus.CANCELLED, o.status());
    }

    @Test
    @DisplayName("Повторная отмена ничего не меняет")
    void repeatedCancellationChangesNothing() {
        Order o = reservedOrder();
        f.cancellationHandler.handle(o);

        assertTrue(f.cancellationHandler.handle(o).isEmpty());
    }

    @Test
    @DisplayName("Отмена заказа без резерва только меняет статус")
    void cancellationWithoutReservationOnlyChangesStatus() {
        Order o = Order.from(order(Marketplace.WB, "1", line("TS-L", 1)));
        f.orders.save(o);

        assertTrue(f.cancellationHandler.handle(o).isEmpty());
        assertEquals(OrderStatus.CANCELLED, o.status());
    }
}
