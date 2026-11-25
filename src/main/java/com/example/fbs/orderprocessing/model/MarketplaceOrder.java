package com.example.fbs.orderprocessing.model;

import java.time.Instant;
import java.util.List;

/**
 * Заказ в том виде, в каком его вернул коннектор площадки
 * (вход A3 «Заказ с маркетплейсов»).
 * cancelled = true означает, что покупатель или площадка отменили заказ.
 */
public record MarketplaceOrder(
        String externalId,
        Marketplace marketplace,
        Instant createdAt,
        List<OrderLine> lines,
        boolean cancelled) {

    public MarketplaceOrder {
        lines = List.copyOf(lines);
    }

    /** Уникальный ключ заказа внутри WMS: номер уникален только в пределах площадки. */
    public String key() {
        return Order.key(marketplace, externalId);
    }
}
