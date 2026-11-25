package com.example.fbs.orderprocessing.model;

/** Резерв товара под заказ (выход A33 «Резерв»). */
public record Reservation(String orderKey, String sku, int quantity) {
}
