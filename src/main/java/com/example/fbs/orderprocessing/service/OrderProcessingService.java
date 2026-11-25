package com.example.fbs.orderprocessing.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import com.example.fbs.orderprocessing.dto.LoadResult;
import com.example.fbs.orderprocessing.dto.ProcessingReport;
import com.example.fbs.orderprocessing.dto.ReservationResult;
import com.example.fbs.orderprocessing.dto.SyncResult;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderLine;
import com.example.fbs.orderprocessing.model.Reservation;
import com.example.fbs.orderprocessing.model.StockUpdate;

/**
 * A3 «Обработать заказ»: связывает A31–A35 так, как нарисовано на диаграмме декомпозиции.
 *
 * Порядок внутри одной площадки: сначала отмены (A32), потом новые заказы (A33),
 * чтобы освобождённый товар сразу можно было отдать под новый заказ.
 * Остатки пересчитываются (A34) и выгружаются (A35) только по затронутым артикулам.
 */
@Service
public class OrderProcessingService {

    private final OrderLoader loader;
    private final CancellationHandler cancellations;
    private final ReservationService reservations;
    private final AvailableStockCalculator calculator;
    private final StockSynchronizer synchronizer;

    private Instant lastRunAt = Instant.EPOCH;
    private ProcessingReport lastReport;

    public OrderProcessingService(OrderLoader loader, CancellationHandler cancellations,
                                  ReservationService reservations, AvailableStockCalculator calculator,
                                  StockSynchronizer synchronizer) {
        this.loader = loader;
        this.cancellations = cancellations;
        this.reservations = reservations;
        this.calculator = calculator;
        this.synchronizer = synchronizer;
    }

    /**
     * Прогон по изменениям с момента прошлого прогона.
     * Вызывается планировщиком и REST-контроллером; synchronized не даёт им запуститься одновременно.
     */
    public synchronized ProcessingReport runSinceLastRun() {
        Instant startedAt = Instant.now();
        ProcessingReport report = process(lastRunAt);
        lastRunAt = startedAt;
        lastReport = report;
        return report;
    }

    public synchronized Optional<ProcessingReport> lastReport() {
        return Optional.ofNullable(lastReport);
    }

    public ProcessingReport process(Instant since) {
        Set<String> affectedSkus = new TreeSet<>();
        int loaded = 0;
        int reserved = 0;
        int notEnough = 0;
        int cancelled = 0;

        for (Marketplace marketplace : Marketplace.values()) {
            LoadResult load = loader.load(marketplace, since);                 // A31
            loaded += load.newOrders().size();

            for (Order order : load.cancelledOrders()) {                       // A32
                List<Reservation> released = cancellations.handle(order);
                released.forEach(r -> affectedSkus.add(r.sku()));
                cancelled++;
            }
            for (Order order : load.newOrders()) {                             // A33
                ReservationResult result = reservations.reserve(order);
                if (result.reserved()) {
                    reserved++;
                    order.lines().stream().map(OrderLine::sku).forEach(affectedSkus::add);
                } else {
                    notEnough++;
                }
            }
        }

        if (affectedSkus.isEmpty()) {
            return new ProcessingReport(loaded, reserved, notEnough, cancelled, List.of(), SyncResult.empty());
        }
        List<StockUpdate> updates = calculator.calculate(affectedSkus);        // A34
        SyncResult sync = synchronizer.sync(updates);                          // A35
        return new ProcessingReport(loaded, reserved, notEnough, cancelled, updates, sync);
    }
}
