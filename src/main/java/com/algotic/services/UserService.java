package com.algotic.services;

import com.algotic.constants.BrokerEnum;
import com.algotic.data.entities.SubscriptionTransactions;
import com.algotic.data.entities.UserActivities;
import com.algotic.model.request.*;
import com.algotic.model.response.*;
import java.util.Date;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ResponseEntity<CreateCustomerResponse> createCustomer(CreateCustomerRequest createCustomerRequest);

    ResponseEntity<GlobalMessageResponse> updateCustomer(String id, UpdateCustomerRequest updateCustomerRequest);

    CustomerResponse getCustomerById(String userId);

    String getAliceBlueToken(String userId);

    String getBrokerSessionId(String userId);

    ResponseEntity<GlobalMessageResponse> saveGstDetails(GstDetailsRequest gstDetailsRequest);

    ResponseEntity<GstResponse> getGstDetails(String userId);

    ResponseEntity<GlobalMessageResponse> getVerified(String verificationCode);

    ResponseEntity<GlobalMessageResponse> verification(EmailRequest emailRequest);

    ResponseEntity<ProfileDetailsResponse> getProfileDetails();

    ResponseEntity<GlobalMessageResponse> activeInactiveUsers(String userId, String type);

    ResponseEntity<InActiveUsersResponse> inactiveUsers(int limit, int offset, String sortBy, String sortOrder);

    ResponseEntity<RenewalHistoryResponse> renewalHistory(String userId);

    ResponseEntity<byte[]> pdfCreate(String userId, SubscriptionTransactions subscriptionTransactions);

    ResponseEntity<GlobalMessageResponse> deleteUser(String id);

    ResponseEntity<List<UserActivities>> userCount(ActiveUsersHistoryRequest activeUsersHistoryRequest);

    ResponseEntity<CustomerManagementResponse> allUsers(
            String name,
            String userId,
            String userType,
            Date lastActiveStartDate,
            Date lastActiveEndDate,
            Date renewalStartDate,
            Date renewalEndDate,
            String status,
            String paymentStatus,
            Integer limit,
            Integer offset);

    ResponseEntity<GlobalMessageResponse> accountDisconnect(String userId);

    ResponseEntity<RegistrationResponse> registration(Integer limit, Integer offset);

    ResponseEntity<GlobalMessageResponse> adminStatus(String userId);

    ResponseEntity<byte[]> userInvoice(Integer subscriptionId);

    ResponseEntity<GlobalMessageResponse> brokerStatus(BrokerCustomerRequest customerBrokerRequest);

    String getMotilalToken(String userId);

    String getSessionToken(String userId, BrokerEnum brokerName);
}
