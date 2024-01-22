package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.model.response.DashboardResponse;
import com.algotic.services.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @GetMapping("/admin/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Admin dashboard",
                        "Process for getting the counts of subscribers, revenue, active users and inactive users",
                        null,
                        null));
        return adminService.adminDashboard();
    }
}
