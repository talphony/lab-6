package com.example.fbs.orderprocessing.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.fbs.orderprocessing.dto.LoadResult;
import com.example.fbs.orderprocessing.integration.ConnectorRegistry;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.MarketplaceOrder;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.repository.OrderRepository;

/**
 * A31 «Загрузить заказы».
 * Вход: заказ с маркетплейсов. Выходы: новый заказ, отмена.
 * Управление: правила маркетплейсов (через коннектор). Механизм: WMS.
 */
@Service
public class OrderLoader {

    private final ConnectorRegistry connectors;
    private final OrderRepository orders;

    public OrderLoader(ConnectorRegistry connectors, OrderRepository orders) {
        this.connectors = connectors;
        this.orders = orders;
    }

    public LoadResult load(Marketplace marketplace, Instant since) {
        List<MarketplaceOrder> fetched = connectors.get(marketplace).fetchNewOrders(since);
        List<Order> newOrders = new ArrayList<>();
        List<Order> cancelled = new ArrayList<>();

        for (MarketplaceOrder incoming : fetched) {
            Optional<Order> existing = orders.findByKey(incoming.key());

            if (incoming.cancelled()) {
                // Отмена имеет смысл только для заказа, который мы уже знаем и ещё не отменили.
                existing.filter(o -> o.status() != OrderStatus.CANCELLED)
                        .ifPresent(cancelled::add);
                continue;
            }
            if (existing.isPresent()) {
                continue; // дубль: площадка прислала заказ повторно
            }
            Order order = Order.from(incoming);
            orders.save(order);
            newOrders.add(order);
        }
        return new LoadResult(newOrders, cancelled);
    }
}
