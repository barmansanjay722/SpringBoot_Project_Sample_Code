package com.algotic.base;

import com.algotic.constants.BrokerEnum;
import com.algotic.services.BrokerMgmtService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class responsible for creating all implemented classes object of  BrokerMgmtService
 *
 */
@Component
public class BrokerMgmtServiceFactory {

    private final Map<BrokerEnum, BrokerMgmtService> brokerServiceMap = new LinkedHashMap<>();

    @Autowired
    protected void addService(List<BrokerMgmtService> brokerService) {
        brokerService.forEach(service -> brokerServiceMap.put(service.getBrokerName(), service));
    }

    public BrokerMgmtService getService(BrokerEnum brokername) {
        return brokerServiceMap.get(brokername);
    }
}
