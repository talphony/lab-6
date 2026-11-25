package com.example.fbs.orderprocessing.dto;

import java.util.List;
import com.example.fbs.orderprocessing.model.StockUpdate;

/** Итог одного прогона блока A3. */
public record ProcessingReport(
        int loaded,
        int reserved,
        int notEnoughStock,
        int cancelled,
        List<StockUpdate> stockUpdates,
        SyncResult syncResult) {

    public ProcessingReport {
        stockUpdates = List.copyOf(stockUpdates);
    }
}
