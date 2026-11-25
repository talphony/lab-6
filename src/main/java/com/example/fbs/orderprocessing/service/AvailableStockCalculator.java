package com.example.fbs.orderprocessing.service;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import com.example.fbs.orderprocessing.config.SafetyBufferPolicy;
import com.example.fbs.orderprocessing.model.StockUpdate;
import com.example.fbs.orderprocessing.repository.ReservationRepository;
import com.example.fbs.orderprocessing.repository.StockRepository;

/**
 * A34 «Пересчитать доступный остаток».
 * Входы: снятый резерв, резерв, актуальные остатки. Выход: доступный остаток.
 * Управление: страховой буфер. Механизм: WMS.
 *
 * Доступно = на складе − резерв − страховой буфер, но не меньше нуля.
 */
@Service
public class AvailableStockCalculator {

    private final StockRepository stock;
    private final ReservationRepository reservations;
    private final SafetyBufferPolicy safetyBuffer;

    public AvailableStockCalculator(StockRepository stock, ReservationRepository reservations,
                                    SafetyBufferPolicy safetyBuffer) {
        this.stock = stock;
        this.reservations = reservations;
        this.safetyBuffer = safetyBuffer;
    }

    public int calculate(String sku) {
        int available = stock.onHand(sku)
                - reservations.reservedQuantity(sku)
                - safetyBuffer.bufferFor(sku);
        return Math.max(0, available);
    }

    /** Пересчёт по набору артикулов; результат отсортирован по артикулу, без повторов. */
    public List<StockUpdate> calculate(Collection<String> skus) {
        return new TreeSet<>(skus).stream()
                .map(sku -> new StockUpdate(sku, calculate(sku)))
                .toList();
    }
}
