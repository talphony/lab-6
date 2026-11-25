package com.example.fbs.orderprocessing.config;

/**
 * Управление «Страховой буфер»: сколько штук не показывать площадкам,
 * чтобы не продать товар, которого нет (пересорт, брак, ошибка пересчёта).
 * В рамках A3 — заглушка.
 */
public interface SafetyBufferPolicy {

    int bufferFor(String sku);
}
