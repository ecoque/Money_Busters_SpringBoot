package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.service.TriggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/triggers")
public class TriggerController {

    private final TriggerService triggerService;

    @Value("${spring.datasource.username}")
    private String currentDbUser;

    public TriggerController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    /* ===========================
       TRIGGER LİSTELEME
       =========================== */

    @GetMapping
    public ResponseEntity<List<TriggerMetadata>> getAllTriggers() {
        return ResponseEntity.ok(triggerService.getAllTriggers());
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<TriggerMetadata>> getTriggersByTable(@PathVariable String tableName) {
        return ResponseEntity.ok(triggerService.getTriggersByTable(tableName));
    }

    @GetMapping("/{triggerName}")
    public ResponseEntity<TriggerMetadata> getTriggerByName(@PathVariable String triggerName) {
        TriggerMetadata trigger = triggerService.getTriggerByName(triggerName);
        return trigger == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(trigger);
    }

    /* ===========================
       TRIGGER ENABLE / DISABLE
       =========================== */

    @PostMapping("/{triggerName}/enable")
    public ResponseEntity<Map<String, String>> enableTrigger(@PathVariable String triggerName) {
        Map<String, String> response = new HashMap<>();
        try {
            triggerService.enableTrigger(triggerName);
            response.put("status", "SUCCESS");
            response.put("message", triggerName + " aktif edildi");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/{triggerName}/disable")
    public ResponseEntity<Map<String, String>> disableTrigger(@PathVariable String triggerName) {
        Map<String, String> response = new HashMap<>();
        try {
            triggerService.disableTrigger(triggerName);
            response.put("status", "SUCCESS");
            response.put("message", triggerName + " pasif edildi");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /* ===========================
       TRIGGER OLUŞTURMA (ASIL İŞ)
       =========================== */

    @PostMapping("/create/{tableName}")
    public ResponseEntity<String> createInsertTrigger(
            @PathVariable String tableName,
            @RequestParam(required = false) String schema
    ) {
        try {
            String targetSchema;

            // 1. Web isteğinde özel şema var mı?
            if (schema != null && !schema.isEmpty()) {
                targetSchema = schema.toUpperCase();
            }
            // 2. Yoksa giriş yapan kullanıcıyı al
            else if (currentDbUser != null && !currentDbUser.isEmpty()) {
                targetSchema = currentDbUser.toUpperCase();
            }
            // 3. O DA YOKSA? Artık "UPT" yok, HATA var!
            else {
                return ResponseEntity.status(401) // 401: Unauthorized (Yetkisiz)
                        .body("HATA: Şema belirlenemedi ve aktif kullanıcı bulunamadı.");
            }

            triggerService.createInsertTrigger(targetSchema, tableName.toUpperCase());

            return ResponseEntity.ok("Trigger oluşturuldu. Şema: " + targetSchema + " Tablo: " + tableName.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }



    /**
     * Belirtilen tablo için tüm otomasyon scriptlerini döndürür.
     */
    @GetMapping("/generate-scripts/{tableName}")
    public ResponseEntity<Map<String, String>> getScripts(@PathVariable String tableName) {
        try {
            Map<String, String> response = triggerService.generateAllScripts(tableName.toUpperCase());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    // TriggerController.java içine eklenebilir

    @GetMapping("/download-script/{tableName}/{type}")
    public ResponseEntity<byte[]> downloadScript(@PathVariable String tableName, @PathVariable String type) {
        Map<String, String> scripts = triggerService.generateAllScripts(tableName.toUpperCase());
        String content = "";
        String fileName = tableName.toLowerCase();

        // İstenen türe göre içeriği seçiyoruz
        if ("main".equalsIgnoreCase(type)) {
            content = scripts.get("main.ddl");
            fileName += ".ddl";
        } else if ("rollback".equalsIgnoreCase(type)) {
            content = scripts.get("rollback[RB].ddl");
            fileName += "[RB].ddl";
        } else if ("trigger".equalsIgnoreCase(type)) {
            content = scripts.get("trigger.trg");
            fileName += ".trg";
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(content.getBytes());
    }

}

