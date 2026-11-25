package com.example.fbs.orderprocessing.demo;

import com.example.fbs.orderprocessing.model.AssemblyTask;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.OrderLine;
import com.example.fbs.orderprocessing.model.OrderStatus;
import com.example.fbs.orderprocessing.model.Reservation;
import com.example.fbs.orderprocessing.model.StockUpdate;
import java.util.List;
import java.util.Map;

/** Всё, что сейчас лежит в заглушках: удобно смотреть до и после прогона A3. */
public record DemoState(
        Map<String, Integer> stockOnHand,
        List<OrderView> orders,
        List<Reservation> reservations,
        List<AssemblyTask> assemblyTasks,
        Map<Marketplace, List<List<StockUpdate>>> pushedStocks) {

    public record OrderView(String key, Marketplace marketplace, String externalId,
                            OrderStatus status, List<OrderLine> lines) {
        static OrderView from(Order o) {
            return new OrderView(o.key(), o.marketplace(), o.externalId(), o.status(), o.lines());
        }
    }
}
