package com.example.fbs.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.example.fbs.orderprocessing.service.Fixture.line;
import static com.example.fbs.orderprocessing.service.Fixture.order;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.Order;
import com.example.fbs.orderprocessing.model.StockUpdate;

/** A34 «Пересчитать доступный остаток». */
class AvailableStockCalculatorTest {

    @Test
    @DisplayName("Доступно = остаток − резерв − буфер")
    void availableIsOnHandMinusReservedMinusBuffer() {
        Fixture f = new Fixture(2);
        f.stock.put("TS-L", 10);
        Order o = Order.from(order(Marketplace.WB, "1", line("TS-L", 3)));
        f.orders.save(o);
        f.reservationService.reserve(o);

        assertEquals(5, f.calculator.calculate("TS-L")); // 10 − 3 − 2
    }

    @Test
    @DisplayName("Доступный остаток не бывает отрицательным")
    void availableIsNeverNegative() {
        Fixture f = new Fixture(5);
        f.stock.put("TS-L", 3);

        assertEquals(0, f.calculator.calculate("TS-L"));
    }

    @Test
    @DisplayName("Неизвестный артикул даёт 0")
    void unknownSkuGivesZero() {
        assertEquals(0, new Fixture().calculator.calculate("NO-SUCH-SKU"));
    }

    @Test
    @DisplayName("Пересчёт по набору: без повторов, по алфавиту")
    void batchCalculationIsDistinctAndSorted() {
        Fixture f = new Fixture();
        f.stock.put("B", 1);
        f.stock.put("A", 2);

        List<StockUpdate> result = f.calculator.calculate(List.of("B", "A", "B"));

        assertEquals(List.of(new StockUpdate("A", 2), new StockUpdate("B", 1)), result);
    }
}
