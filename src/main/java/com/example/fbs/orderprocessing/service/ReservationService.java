package com.example.fbs.orderprocessing.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.example.fbs.orderprocessing.dto.ReservationResult;
import com.example.fbs.orderprocessing.integration.AssemblyTaskPublisher;
import com.example.fbs.orderprocessing.model.AssemblyTask;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderLine;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.model.Reservation;
import com.example.fbs.orderprocessing.repository.OrderRepository;
import com.example.fbs.orderprocessing.repository.ReservationRepository;
import com.example.fbs.orderprocessing.repository.StockRepository;

/**
 * A33 «Зарезервировать товар».
 * Входы: новый заказ, актуальные остатки. Выходы: резерв, задание на сборку.
 * Управление: правила маркетплейсов. Механизм: WMS.
 *
 * Резерв ставится по принципу «всё или ничего»: если хотя бы одной позиции не хватает,
 * заказ уходит в NOT_ENOUGH_STOCK и ничего не резервируется.
 * Проверка идёт по свободному остатку (на складе − резерв), без страхового буфера:
 * буфер защищает от продаж на площадке, а заказ уже продан.
 */
@Service
public class ReservationService {

    private final StockRepository stock;
    private final ReservationRepository reservations;
    private final OrderRepository orders;
    private final AssemblyTaskPublisher assemblyTasks;

    public ReservationService(StockRepository stock, ReservationRepository reservations,
                              OrderRepository orders, AssemblyTaskPublisher assemblyTasks) {
        this.stock = stock;
        this.reservations = reservations;
        this.orders = orders;
        this.assemblyTasks = assemblyTasks;
    }

    public ReservationResult reserve(Order order) {
        if (order.status() != OrderStatus.NEW) {
            throw new IllegalStateException("Резервировать можно только новый заказ: " + order);
        }

        Map<String, Integer> needed = sumBySku(order.lines());
        List<String> shortage = new ArrayList<>();
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            int free = stock.onHand(e.getKey()) - reservations.reservedQuantity(e.getKey());
            if (free < e.getValue()) {
                shortage.add(e.getKey());
            }
        }

        if (!shortage.isEmpty()) {
            order.markNotEnoughStock();
            orders.save(order);
            return new ReservationResult(order, false, shortage);
        }

        List<Reservation> created = needed.entrySet().stream()
                .map(e -> new Reservation(order.key(), e.getKey(), e.getValue()))
                .toList();
        reservations.saveAll(created);
        order.markReserved();
        orders.save(order);
        assemblyTasks.publish(new AssemblyTask(order.key(), order.marketplace(), order.lines()));
        return new ReservationResult(order, true, List.of());
    }

    /** Одна и та же позиция может встретиться в заказе несколько раз. */
    private static Map<String, Integer> sumBySku(List<OrderLine> lines) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            result.merge(line.sku(), line.quantity(), Integer::sum);
        }
        return result;
    }
}
