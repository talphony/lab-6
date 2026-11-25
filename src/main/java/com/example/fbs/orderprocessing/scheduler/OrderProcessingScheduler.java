package com.example.fbs.orderprocessing.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.fbs.orderprocessing.service.OrderProcessingService;

/**
 * Компонент «Планировщик» с C4 L3: запускает A3 по расписанию.
 * Интервал задаётся настройкой wms.order-processing.interval (по умолчанию 2 минуты).
 * Выключается настройкой wms.order-processing.scheduler-enabled=false.
 */
@Component
@ConditionalOnProperty(name = "wms.order-processing.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class OrderProcessingScheduler {

    private final OrderProcessingService processing;

    public OrderProcessingScheduler(OrderProcessingService processing) {
        this.processing = processing;
    }

    @Scheduled(fixedDelayString = "${wms.order-processing.interval:PT2M}",
            initialDelayString = "${wms.order-processing.interval:PT2M}")
    public void run() {
        processing.runSinceLastRun();
    }
}
