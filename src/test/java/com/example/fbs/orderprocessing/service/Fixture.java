package com.example.fbs.orderprocessing.service;

import java.time.Instant;
import java.util.List;
import com.example.fbs.orderprocessing.config.DefaultMarketplaceRules;
import com.example.fbs.orderprocessing.config.FixedSafetyBufferPolicy;
import com.example.fbs.orderprocessing.integration.CollectingAssemblyTaskPublisher;
import com.example.fbs.orderprocessing.integration.ConnectorRegistry;
import com.example.fbs.orderprocessing.integration.StubMarketplaceConnector;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.MarketplaceOrder;
import com.example.fbs.orderprocessing.model.OrderLine;
import com.example.fbs.orderprocessing.repository.InMemoryOrderRepository;
import com.example.fbs.orderprocessing.repository.InMemoryReservationRepository;
import com.example.fbs.orderprocessing.repository.InMemoryStockRepository;

/** Собирает блок A3 на заглушках без Spring-контекста. */
class Fixture {

    static final Instant SINCE = Instant.parse("2026-11-25T00:00:00Z");

    final StubMarketplaceConnector wb = new StubMarketplaceConnector(Marketplace.WB);
    final StubMarketplaceConnector ozon = new StubMarketplaceConnector(Marketplace.OZON);
    final StubMarketplaceConnector ym = new StubMarketplaceConnector(Marketplace.YANDEX_MARKET);
    final ConnectorRegistry registry = new ConnectorRegistry(List.of(wb, ozon, ym));

    final InMemoryStockRepository stock = new InMemoryStockRepository();
    final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
    final CollectingAssemblyTaskPublisher assemblyTasks = new CollectingAssemblyTaskPublisher();
    final DefaultMarketplaceRules rules = new DefaultMarketplaceRules();
    final FixedSafetyBufferPolicy buffer;

    final OrderLoader loader;
    final CancellationHandler cancellationHandler;
    final ReservationService reservationService;
    final AvailableStockCalculator calculator;
    final StockSynchronizer synchronizer;
    final OrderProcessingService processing;

    Fixture() {
        this(0);
    }

    Fixture(int safetyBuffer) {
        buffer = new FixedSafetyBufferPolicy(safetyBuffer);
        loader = new OrderLoader(registry, orders);
        cancellationHandler = new CancellationHandler(reservations, orders);
        reservationService = new ReservationService(stock, reservations, orders, assemblyTasks);
        calculator = new AvailableStockCalculator(stock, reservations, buffer);
        synchronizer = new StockSynchronizer(registry, rules);
        processing = new OrderProcessingService(loader, cancellationHandler, reservationService,
                calculator, synchronizer);
    }

    static MarketplaceOrder order(Marketplace m, String id, OrderLine... lines) {
        return new MarketplaceOrder(id, m, SINCE, List.of(lines), false);
    }

    static MarketplaceOrder cancel(Marketplace m, String id, OrderLine... lines) {
        return new MarketplaceOrder(id, m, SINCE, List.of(lines), true);
    }

    static OrderLine line(String sku, int qty) {
        return new OrderLine(sku, qty);
    }
}
