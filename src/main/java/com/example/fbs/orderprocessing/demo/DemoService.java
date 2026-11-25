package com.example.fbs.orderprocessing.demo;

import com.example.fbs.orderprocessing.integration.CollectingAssemblyTaskPublisher;
import com.example.fbs.orderprocessing.integration.StubMarketplaceConnector;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.MarketplaceOrder;
import com.example.fbs.orderprocessing.model.OrderLine;
import com.example.fbs.orderprocessing.model.StockUpdate;
import com.example.fbs.orderprocessing.repository.InMemoryOrderRepository;
import com.example.fbs.orderprocessing.repository.InMemoryReservationRepository;
import com.example.fbs.orderprocessing.repository.InMemoryStockRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Демо-режим для ручной проверки A3: наполняет заглушки данными и показывает их состояние.
 * Включается настройкой wms.demo.enabled=true. В рабочей системе выключается.
 */
@Service
@ConditionalOnProperty(name = "wms.demo.enabled", havingValue = "true")
public class DemoService {

    private final InMemoryStockRepository stock;
    private final InMemoryOrderRepository orders;
    private final InMemoryReservationRepository reservations;
    private final CollectingAssemblyTaskPublisher assemblyTasks;
    private final Map<Marketplace, StubMarketplaceConnector> connectors = new EnumMap<>(Marketplace.class);

    public DemoService(InMemoryStockRepository stock, InMemoryOrderRepository orders,
                       InMemoryReservationRepository reservations,
                       CollectingAssemblyTaskPublisher assemblyTasks,
                       List<StubMarketplaceConnector> stubConnectors) {
        this.stock = stock;
        this.orders = orders;
        this.reservations = reservations;
        this.assemblyTasks = assemblyTasks;
        stubConnectors.forEach(c -> connectors.put(c.marketplace(), c));
    }

    /** Очистить всё: остатки, заказы, резервы, задания, очереди площадок. */
    public void reset() {
        stock.clear();
        orders.clear();
        reservations.clear();
        assemblyTasks.clear();
        connectors.values().forEach(StubMarketplaceConnector::clear);
    }

    /**
     * Готовый сценарий: 4 товара на складе и 4 заказа с трёх площадок.
     * Заказ Ozon на кружки специально больше остатка, чтобы показать нехватку.
     */
    public void seed() {
        reset();
        setStock("TS-OVR-BLK-L", 10);   // Футболка оверсайз, чёрная, L
        setStock("MUG-350-WHT", 2);     // Кружка керамическая 350 мл
        setStock("CASE-IP15-CLR", 120); // Чехол iPhone 15
        setStock("HD-BAS-GRY-M", 7);    // Худи базовое, серое, M

        addOrder(Marketplace.WB, "WB-1001", List.of(new OrderLine("TS-OVR-BLK-L", 3)));
        addOrder(Marketplace.OZON, "OZ-2001", List.of(new OrderLine("MUG-350-WHT", 5)));
        addOrder(Marketplace.YANDEX_MARKET, "YM-3001",
                List.of(new OrderLine("CASE-IP15-CLR", 1), new OrderLine("TS-OVR-BLK-L", 1)));
        addOrder(Marketplace.WB, "WB-1002", List.of(new OrderLine("HD-BAS-GRY-M", 2)));
    }

    public void setStock(String sku, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Остаток не может быть отрицательным");
        }
        stock.put(sku, quantity);
    }

    /** Имитирует новый заказ на площадке: его заберёт A31 при следующем прогоне. */
    public void addOrder(Marketplace marketplace, String externalId, List<OrderLine> lines) {
        connectors.get(marketplace).enqueue(
                new MarketplaceOrder(externalId, marketplace, Instant.now(), lines, false));
    }

    /** Имитирует отмену заказа покупателем на площадке. */
    public void cancelOrder(Marketplace marketplace, String externalId) {
        connectors.get(marketplace).enqueue(
                new MarketplaceOrder(externalId, marketplace, Instant.now(), List.of(), true));
    }

    public DemoState state() {
        Map<Marketplace, List<List<StockUpdate>>> pushed =
                new EnumMap<>(Marketplace.class);
        connectors.forEach((m, c) -> pushed.put(m, c.pushedBatches()));
        return new DemoState(
                stock.snapshot(),
                orders.findAll().stream().map(DemoState.OrderView::from).toList(),
                reservations.findAll(),
                assemblyTasks.published(),
                pushed);
    }
}
