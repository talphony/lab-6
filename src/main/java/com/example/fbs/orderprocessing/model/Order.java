package com.example.fbs.orderprocessing.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Заказ внутри WMS. */
public class Order {

    private final String externalId;
    private final Marketplace marketplace;
    private final Instant createdAt;
    private final List<OrderLine> lines;
    private OrderStatus status;

    public Order(String externalId, Marketplace marketplace, Instant createdAt,
                 List<OrderLine> lines, OrderStatus status) {
        this.externalId = Objects.requireNonNull(externalId);
        this.marketplace = Objects.requireNonNull(marketplace);
        this.createdAt = createdAt;
        this.lines = List.copyOf(lines);
        this.status = Objects.requireNonNull(status);
    }

    public static Order from(MarketplaceOrder source) {
        return new Order(source.externalId(), source.marketplace(), source.createdAt(),
                source.lines(), OrderStatus.NEW);
    }

    public static String key(Marketplace marketplace, String externalId) {
        return marketplace + ":" + externalId;
    }

    public String key() {
        return key(marketplace, externalId);
    }

    public void markReserved() {
        requireStatus(OrderStatus.NEW);
        status = OrderStatus.RESERVED;
    }

    public void markNotEnoughStock() {
        requireStatus(OrderStatus.NEW);
        status = OrderStatus.NOT_ENOUGH_STOCK;
    }

    public void markCancelled() {
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Заказ " + key() + " в статусе " + status + ", ожидался " + expected);
        }
    }

    public String externalId() { return externalId; }
    public Marketplace marketplace() { return marketplace; }
    public Instant createdAt() { return createdAt; }
    public List<OrderLine> lines() { return lines; }
    public OrderStatus status() { return status; }

    @Override
    public String toString() {
        return "Order[" + key() + ", " + status + ", " + lines + "]";
    }
}
