package com.example.fbs.orderprocessing.config;

import com.example.fbs.orderprocessing.model.Marketplace;

/** Управление «Правила маркетплейсов»: ограничения API площадок. В рамках A3 — заглушка. */
public interface MarketplaceRules {

    /** Сколько позиций остатков площадка принимает за один запрос. */
    int stockBatchSize(Marketplace marketplace);
}
