package com.algotic.base;

import com.algotic.adapter.StockMarketBroker;
import com.algotic.constants.BrokerEnum;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BrokerAdapterFactory {

    private final Map<BrokerEnum, StockMarketBroker> brokerAdapterServiceMap = new LinkedHashMap<>();

    @Autowired
    protected void addService(List<StockMarketBroker> stockMarketBroker) {
        stockMarketBroker.forEach(service -> brokerAdapterServiceMap.put(service.getBrokerName(), service));
    }

    public StockMarketBroker getAdapter(@NotNull BrokerEnum brokername) {
        return brokerAdapterServiceMap.get(brokername);
    }
}
