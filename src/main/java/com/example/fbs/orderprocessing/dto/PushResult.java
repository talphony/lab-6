package com.example.fbs.orderprocessing.dto;

import java.util.List;

/** Ответ площадки на выгрузку пачки остатков. */
public record PushResult(int accepted, List<String> errors) {
    public PushResult {
        errors = List.copyOf(errors);
    }

    public static PushResult ok(int accepted) {
        return new PushResult(accepted, List.of());
    }
}
