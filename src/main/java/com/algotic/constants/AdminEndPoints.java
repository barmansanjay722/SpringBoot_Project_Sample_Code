package com.algotic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AdminEndPoints {
    DASHBOARD("/api/admin/dashboard"),
    REPORTS_RENEWAL("/api/reports/renewal"),
    REPORT_STRATEGY("/api/reports/strategy"),
    REPORT_TRADES("/api/reports/trades"),
    REPORT_REGISTRATION("/api/reports/registration"),
    STRATEGIES("/api/strategies"),
    STRATEGY("/api/strategy"),
    GET_STRATEGY("/api/strategy/{id}"),
    ACTIVE_INACTIVE_STRATEGY("/api/strategy/{id}/{type}"),
    DELETE_STRATEGY("/api/strategy/{id}"),
    USER_ID("/api/customer/{id}"),
    USER_HISTORY("/api/customer/activeUser/history"),
    USER_MANAGEMENT("/api/customer/management"),
    USER_SUBSCRIPTION("/api/customer/{userId}/subscription"),
    USER_ACCOUNT("/api/customer/{userId}/account/disconnect"),
    USER_INVOICE("/api/customer/{userId}/invoice"),
    USER_APPROVE("/api/customer/{userId}/approve"),
    INACTIVE_USER("/api/customer/inactiveUser"),
    USER_ACTIVE_INACTIVE("/api/customer/{userId}/{type}");
    private String endPoint;
}
