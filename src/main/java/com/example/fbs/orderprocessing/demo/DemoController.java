package com.example.fbs.orderprocessing.demo;

import com.example.fbs.orderprocessing.model.Marketplace;
import com.example.fbs.orderprocessing.model.OrderLine;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ручная проверка A3: подготовить данные, затем вызвать POST /api/order-processing/run. */
@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "wms.demo.enabled", havingValue = "true")
public class DemoController {

    private final DemoService demo;

    public DemoController(DemoService demo) {
        this.demo = demo;
    }

    public record StockRequest(String sku, int quantity) {
    }

    public record OrderRequest(Marketplace marketplace, String externalId, List<OrderLine> lines) {
    }

    @PostMapping("/seed")
    public DemoState seed() {
        demo.seed();
        return demo.state();
    }

    @PostMapping("/reset")
    public DemoState reset() {
        demo.reset();
        return demo.state();
    }

    @PutMapping("/stock")
    public DemoState setStock(@RequestBody StockRequest request) {
        demo.setStock(request.sku(), request.quantity());
        return demo.state();
    }

    @PostMapping("/orders")
    public DemoState addOrder(@RequestBody OrderRequest request) {
        demo.addOrder(request.marketplace(), request.externalId(), request.lines());
        return demo.state();
    }

    @PostMapping("/orders/{marketplace}/{externalId}/cancel")
    public DemoState cancel(@PathVariable Marketplace marketplace, @PathVariable String externalId) {
        demo.cancelOrder(marketplace, externalId);
        return demo.state();
    }

    @GetMapping("/state")
    public DemoState state() {
        return demo.state();
    }
}
