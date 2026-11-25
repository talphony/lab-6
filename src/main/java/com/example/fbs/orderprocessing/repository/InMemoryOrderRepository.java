package com.example.fbs.orderprocessing.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import com.example.fbs.orderprocessing.model.Order;

/** Заглушка хранилища заказов в памяти. */
@Component
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findByKey(String key) {
        return Optional.ofNullable(orders.get(key));
    }

    public List<Order> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::key))
                .toList();
    }

    public void clear() {
        orders.clear();
    }

    @Override
    public void save(Order order) {
        orders.put(order.key(), order);
    }
}
