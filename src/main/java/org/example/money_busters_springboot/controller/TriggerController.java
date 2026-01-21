package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.service.TriggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trigger işlemleri için REST controller
 */
@RestController
@RequestMapping("/api/triggers")
public class TriggerController {

    private final TriggerService triggerService;

    public TriggerController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    /**
     * Tüm trigger'ları listeler
     * GET /api/triggers
     */
    @GetMapping
    public ResponseEntity<List<TriggerMetadata>> getAllTriggers() {
        List<TriggerMetadata> triggers = triggerService.getAllTriggers();
        return ResponseEntity.ok(triggers);
    }

    /**
     * Belirli bir tabloya ait trigger'ları listeler
     * GET /api/triggers/table/{tableName}
     */
    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<TriggerMetadata>> getTriggersByTable(@PathVariable String tableName) {
        List<TriggerMetadata> triggers = triggerService.getTriggersByTable(tableName);
        return ResponseEntity.ok(triggers);
    }

    /**
     * Trigger adına göre trigger bilgisini getirir
     * GET /api/triggers/{triggerName}
     */
    @GetMapping("/{triggerName}")
    public ResponseEntity<TriggerMetadata> getTriggerByName(@PathVariable String triggerName) {
        TriggerMetadata trigger = triggerService.getTriggerByName(triggerName);
        if (trigger == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trigger);
    }

    /**
     * Trigger'ı aktif yapar
     * POST /api/triggers/{triggerName}/enable
     */
    @PostMapping("/{triggerName}/enable")
    public ResponseEntity<Map<String, String>> enableTrigger(@PathVariable String triggerName) {
        Map<String, String> response = new HashMap<>();
        try {
            triggerService.enableTrigger(triggerName);
            response.put("status", "SUCCESS");
            response.put("message", triggerName + " trigger'ı aktif edildi.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Trigger aktif edilemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Trigger'ı pasif yapar
     * POST /api/triggers/{triggerName}/disable
     */
    @PostMapping("/{triggerName}/disable")
    public ResponseEntity<Map<String, String>> disableTrigger(@PathVariable String triggerName) {
        Map<String, String> response = new HashMap<>();
        try {
            triggerService.disableTrigger(triggerName);
            response.put("status", "SUCCESS");
            response.put("message", triggerName + " trigger'ı pasif edildi.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Trigger pasif edilemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
