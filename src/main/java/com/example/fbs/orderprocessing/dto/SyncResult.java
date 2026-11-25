package com.example.fbs.orderprocessing.dto;

import java.util.List;
import java.util.Map;
import com.example.fbs.orderprocessing.model.Marketplace;

/** Результат A35: сколько позиций принято каждой площадкой, сколько пачек, какие ошибки. */
public record SyncResult(Map<Marketplace, Integer> acceptedByMarketplace, int batches, List<String> errors) {
    public SyncResult {
        acceptedByMarketplace = Map.copyOf(acceptedByMarketplace);
        errors = List.copyOf(errors);
    }

    public static SyncResult empty() {
        return new SyncResult(Map.of(), 0, List.of());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
