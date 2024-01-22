package com.algotic.services;

import com.algotic.model.response.DashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AdminService {
    ResponseEntity<DashboardResponse> adminDashboard();
}
