package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.service.DatabaseConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Database bağlantısını test etmek için controller
 */
@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private final DatabaseConnectionService databaseConnectionService;

    public DatabaseHealthController(DatabaseConnectionService databaseConnectionService) {
        this.databaseConnectionService = databaseConnectionService;
    }

    /**
     * Database bağlantısını test eder
     * GET /api/health/db
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isConnected = databaseConnectionService.testConnection();
            String currentUser = databaseConnectionService.getCurrentUser();

            response.put("status", "SUCCESS");
            response.put("connected", isConnected);
            response.put("currentUser", currentUser);
            response.put("message", "Database bağlantısı başarılı!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("connected", false);
            response.put("message", "Database bağlantısı başarısız: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Detaylı database bilgisi
     * GET /api/health/db/details
     */
    @GetMapping("/db/details")
    public ResponseEntity<Map<String, Object>> getDatabaseDetails() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", "SUCCESS");
            response.put("currentUser", databaseConnectionService.getCurrentUser());
            response.put("databaseVersion", databaseConnectionService.getDatabaseVersion());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Detaylar alınamadı: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
