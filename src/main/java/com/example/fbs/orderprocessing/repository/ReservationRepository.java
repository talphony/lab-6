package com.example.fbs.orderprocessing.repository;

import java.util.List;
import com.example.fbs.orderprocessing.model.Reservation;

/** Хранилище резервов. В рамках A3 — заглушка. */
public interface ReservationRepository {

    void saveAll(List<Reservation> reservations);

    /** Удаляет все резервы заказа и возвращает их (выход A32 «Снятый резерв»). */
    List<Reservation> removeByOrder(String orderKey);

    /** Сколько штук артикула зарезервировано под все заказы. */
    int reservedQuantity(String sku);
}
