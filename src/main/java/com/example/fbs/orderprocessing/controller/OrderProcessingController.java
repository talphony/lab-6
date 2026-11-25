package com.example.fbs.orderprocessing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.fbs.orderprocessing.dto.ProcessingReport;
import com.example.fbs.orderprocessing.service.OrderProcessingService;

/**
 * REST-доступ к блоку A3: ручной запуск обработки заказов и последний отчёт.
 * Бизнес-логики здесь нет, только вызов сервиса.
 */
@RestController
@RequestMapping("/api/order-processing")
public class OrderProcessingController {

    private final OrderProcessingService processing;

    public OrderProcessingController(OrderProcessingService processing) {
        this.processing = processing;
    }

    /** Запустить A3 вне расписания, например кнопкой «Обновить» на экране заказов. */
    @PostMapping("/run")
    public ProcessingReport run() {
        return processing.runSinceLastRun();
    }

    /** Отчёт последнего прогона; 204, если прогонов ещё не было. */
    @GetMapping("/last-report")
    public ResponseEntity<ProcessingReport> lastReport() {
        return processing.lastReport()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
