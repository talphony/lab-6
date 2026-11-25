package com.example.fbs.orderprocessing.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.model.Reservation;
import com.example.fbs.orderprocessing.repository.OrderRepository;
import com.example.fbs.orderprocessing.repository.ReservationRepository;

/**
 * A32 «Обработать отмену».
 * Вход: отмена. Выход: снятый резерв.
 * Управление: правила маркетплейсов. Механизм: WMS.
 */
@Service
public class CancellationHandler {

    private final ReservationRepository reservations;
    private final OrderRepository orders;

    public CancellationHandler(ReservationRepository reservations, OrderRepository orders) {
        this.reservations = reservations;
        this.orders = orders;
    }

    /** Снимает резерв заказа и переводит его в CANCELLED. Повторный вызов ничего не делает. */
    public List<Reservation> handle(Order order) {
        if (order.status() == OrderStatus.CANCELLED) {
            return List.of();
        }
        List<Reservation> released = reservations.removeByOrder(order.key());
        order.markCancelled();
        orders.save(order);
        return released;
    }
}
