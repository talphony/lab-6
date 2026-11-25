package com.example.fbs.orderprocessing.repository;

/**
 * Фактический остаток на складе (вход A3 «Актуальные остатки», его ведёт блок A2).
 * В рамках A3 — заглушка.
 */
public interface StockRepository {

    /** Сколько штук артикула лежит в ячейках. */
    int onHand(String sku);
}
