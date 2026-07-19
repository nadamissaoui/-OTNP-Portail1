package com.monitoring.controller;

// Remplacement de com.esprit.monitoring par com.monitoring
import com.monitoring.dto.DashboardKpiDTO;
import com.monitoring.service.ProcessMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "http://localhost:3000")
public class KPIController {

    @Autowired
    private ProcessMonitoringService monitoringService;

    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpiDTO> getDashboardKPIs() {
        DashboardKpiDTO kpis = monitoringService.getLiveDashboardKPIs();
        return ResponseEntity.ok(kpis);
    }
}