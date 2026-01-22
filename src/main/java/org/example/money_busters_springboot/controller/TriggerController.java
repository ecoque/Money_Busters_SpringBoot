package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.service.TriggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/triggers")
public class TriggerController {

    private final TriggerService triggerService;

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
    public ResponseEntity<String> createInsertTrigger(@PathVariable String tableName) {
        try {
            triggerService.createInsertTrigger("UPT", tableName.toUpperCase());
            return ResponseEntity.ok("Trigger oluşturuldu: TRG_" + tableName);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Hata: " + e.getMessage());
        }
    }
}
