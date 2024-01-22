package com.algotic.services;

import com.algotic.constants.BrokerEnum;
import com.algotic.model.request.IBrokerSessionRequest;
import com.algotic.model.response.GlobalMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * This interface is used to do crud operations related to brokers inside algotic
 *
 */
@Service
public interface BrokerMgmtService {
    ResponseEntity<GlobalMessageResponse> saveCustomerBrokerDetails(IBrokerSessionRequest customerBrokerRequest);

    BrokerEnum getBrokerName();
}
