package com.example.fbs.orderprocessing.repository;

import java.util.Optional;
import com.example.fbs.orderprocessing.model.Order;

/** Хранилище заказов. В рамках A3 — заглушка. */
public interface OrderRepository {

    Optional<Order> findByKey(String key);

    void save(Order order);
}
