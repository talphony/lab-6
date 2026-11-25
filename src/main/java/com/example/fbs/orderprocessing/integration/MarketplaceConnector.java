package com.example.fbs.orderprocessing.integration;

import java.time.Instant;
import java.util.List;
import com.example.fbs.orderprocessing.dto.PushResult;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.MarketplaceOrder;
import com.example.fbs.orderprocessing.model.StockUpdate;

/**
 * Коннектор площадки (механизм «WMS» на IDEF0, компонент «Коннекторы маркетплейсов» на C4 L3/L4).
 * В рамках A3 подменяется заглушкой: реальные HTTP-адаптеры WB, Ozon и ЯМ — отдельная задача.
 */
public interface MarketplaceConnector {

    Marketplace marketplace();

    /** Новые и отменённые заказы, изменившиеся после since. */
    List<MarketplaceOrder> fetchNewOrders(Instant since);

    /** Выгрузка одной пачки остатков. */
    PushResult pushStocks(List<StockUpdate> batch);
}
