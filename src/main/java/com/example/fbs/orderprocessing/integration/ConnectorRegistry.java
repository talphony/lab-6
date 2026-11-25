package com.example.fbs.orderprocessing.integration;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.example.fbs.orderprocessing.model.Marketplace;

/** Реестр коннекторов: выдаёт коннектор по площадке. */
@Component
public class ConnectorRegistry {

    private final Map<Marketplace, MarketplaceConnector> connectors = new EnumMap<>(Marketplace.class);

    public ConnectorRegistry(List<MarketplaceConnector> all) {
        for (MarketplaceConnector c : all) {
            if (connectors.put(c.marketplace(), c) != null) {
                throw new IllegalStateException("Два коннектора для площадки " + c.marketplace());
            }
        }
    }

    public MarketplaceConnector get(Marketplace marketplace) {
        MarketplaceConnector c = connectors.get(marketplace);
        if (c == null) {
            throw new IllegalArgumentException("Нет коннектора для площадки " + marketplace);
        }
        return c;
    }

    public Collection<MarketplaceConnector> all() {
        return connectors.values();
    }
}
