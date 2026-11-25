package com.example.fbs.orderprocessing.model;

/** Строка заказа: какой товар и сколько штук. */
public record OrderLine(String sku, int quantity) {
    public OrderLine {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("Артикул не задан");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0: " + sku);
        }
    }
}
