package com.example.fbs.orderprocessing.model;

import java.util.List;

/** Задание на сборку (выход A3, вход A4 «Собрать и упаковать заказ»). */
public record AssemblyTask(String orderKey, Marketplace marketplace, List<OrderLine> lines) {
    public AssemblyTask {
        lines = List.copyOf(lines);
    }
}
