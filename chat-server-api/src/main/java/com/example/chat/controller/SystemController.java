package com.example.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.management.OperatingSystemMXBean;

@RestController
@RequestMapping("/system")
public class SystemController {

    @GetMapping("/health_check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "OK");
        body.put("message", "System is healthy");
        body.put("ipAddress", getLocalIp());
        body.put("timestamp", LocalDateTime.now());
        body.put("cpuUsage", getCurrentCpuUsage());
        body.put("branch", "2_monolithic_cloud");

        return ResponseEntity.ok(body);
    }

    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private double getCurrentCpuUsage() {
        try {
            OperatingSystemMXBean osBean =
                    (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double load = osBean.getSystemCpuLoad(); // 0.0 ~ 1.0 또는 -1.0 (지원 안할 경우)
            if (load < 0) return -1.0;
            return Math.round(load * 10000.0) / 100.0; // 소수점 두 자리, 퍼센트(예: 12.34)
        } catch (Throwable t) {
            return -1.0;
        }
    }
}