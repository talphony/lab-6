package com.example.fbs.orderprocessing.model;

/** Доступный остаток по артикулу (выход A34 «Доступный остаток»). */
public record StockUpdate(String sku, int available) {
}
