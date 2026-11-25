package com.example.fbs.orderprocessing.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.example.fbs.orderprocessing.model.Reservation;

/** Заглушка хранилища резервов в памяти. */
@Component
public class InMemoryReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public synchronized void saveAll(List<Reservation> items) {
        reservations.addAll(items);
    }

    @Override
    public synchronized List<Reservation> removeByOrder(String orderKey) {
        List<Reservation> removed = reservations.stream()
                .filter(r -> r.orderKey().equals(orderKey))
                .toList();
        reservations.removeAll(removed);
        return removed;
    }

    public synchronized List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    public synchronized void clear() {
        reservations.clear();
    }

    @Override
    public synchronized int reservedQuantity(String sku) {
        return reservations.stream()
                .filter(r -> r.sku().equals(sku))
                .mapToInt(Reservation::quantity)
                .sum();
    }
}
