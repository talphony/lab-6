package com.example.fbs.orderprocessing.dto;

import java.util.List;
import com.example.fbs.orderprocessing.model.Order;

/** Результат A33. shortageSkus — артикулы, которых не хватило. */
public record ReservationResult(Order order, boolean reserved, List<String> shortageSkus) {
    public ReservationResult {
        shortageSkus = List.copyOf(shortageSkus);
    }
}
