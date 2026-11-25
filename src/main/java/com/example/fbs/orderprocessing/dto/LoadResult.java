package com.example.fbs.orderprocessing.dto;

import java.util.List;
import com.example.fbs.orderprocessing.model.Order;

/** Результат A31: новые заказы и заказы, которые площадка отменила. */
public record LoadResult(List<Order> newOrders, List<Order> cancelledOrders) {
    public LoadResult {
        newOrders = List.copyOf(newOrders);
        cancelledOrders = List.copyOf(cancelledOrders);
    }
}
