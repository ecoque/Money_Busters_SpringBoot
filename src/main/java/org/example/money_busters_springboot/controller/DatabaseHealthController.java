package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.dto.GenericTableDataDTO;
import org.example.money_busters_springboot.service.DatabaseConnectionService;
import org.example.money_busters_springboot.service.GenericTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database bağlantısını test etmek için controller
 */
@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private final DatabaseConnectionService databaseConnectionService;
    private final GenericTableService genericTableService;

    public DatabaseHealthController(DatabaseConnectionService databaseConnectionService,
                                    GenericTableService genericTableService) {
        this.databaseConnectionService = databaseConnectionService;
        this.genericTableService = genericTableService;
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

    /**
     * Kullanıcıya ait tüm tabloları listeler
     * GET /api/health/tables
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getAllTables() {
        Map<String, Object> response = new HashMap<>();

        try {
            String currentUser = databaseConnectionService.getCurrentUser();
            List<String> tables = genericTableService.getUserTables(currentUser);

            response.put("status", "SUCCESS");
            response.put("currentUser", currentUser);
            response.put("tableCount", tables.size());
            response.put("tables", tables);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Tablolar listelenemedi: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Generic tablo verilerini getirir
     * GET /api/health/table/{tableName}
     *
     * Örnek: GET /api/health/table/DENEME_MUSTERI
     */
    @GetMapping("/table/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableData(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();

        try {
            GenericTableDataDTO tableData = genericTableService.getTableData(tableName);

            response.put("status", "SUCCESS");
            response.put("data", tableData);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "ERROR");
            response.put("message", "Geçersiz tablo adı: " + tableName);

            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Tablo verileri alınamadı: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Tablodaki kayıt sayısını döner
     * GET /api/health/table/{tableName}/count
     */
    @GetMapping("/table/{tableName}/count")
    public ResponseEntity<Map<String, Object>> getTableRowCount(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();

        try {
            long count = genericTableService.getTableRowCount(tableName);

            response.put("status", "SUCCESS");
            response.put("tableName", tableName);
            response.put("rowCount", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Kayıt sayısı alınamadı: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
