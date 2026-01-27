package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.jdbc.core.JdbcTemplate;


import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TriggerService {

    private final TriggerRepository triggerRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TriggerGeneratorService triggerGeneratorService;

    public TriggerService(TriggerRepository triggerRepository,
                          JdbcTemplate jdbcTemplate,
                          TriggerGeneratorService triggerGeneratorService) {
        this.triggerRepository = triggerRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.triggerGeneratorService = triggerGeneratorService;
    }

    /* ===========================
       TRIGGER LİSTELEME
       =========================== */

    public List<TriggerMetadata> getAllTriggers() {
        return triggerRepository.findAllTriggers();
    }

    public List<TriggerMetadata> getTriggersByTable(String tableName) {
        return triggerRepository.findTriggersByTableName(tableName);
    }

    public TriggerMetadata getTriggerByName(String triggerName) {
        return triggerRepository.findTriggerByName(triggerName);
    }

    /* ===========================
       TRIGGER ENABLE / DISABLE
       =========================== */

    public void enableTrigger(String triggerName) {
        triggerRepository.setTriggerStatus(triggerName, true);
    }

    public void disableTrigger(String triggerName) {
        triggerRepository.setTriggerStatus(triggerName, false);
    }

    /* ===========================
       TRIGGER OLUŞTURMA
       =========================== */

    public void createInsertTrigger(String schema, String tableName) {
        String sql = triggerGeneratorService.generateFullTriggerSql(schema, tableName);
        jdbcTemplate.execute(sql);
    }

    public Map<String, String> generateAllScripts(String tableName) {
        String schema = "UPT"; // Task gereği sabitlendi
        Map<String, String> scripts = new HashMap<>();

        // Bu metodlar TriggerGeneratorService içinde tanımlanmış olmalıdır.
        scripts.put("main.ddl", triggerGeneratorService.generateHisTableDdl(schema, tableName));
        scripts.put("rollback[RB].ddl", triggerGeneratorService.generateRollbackDdl(schema, tableName));
        scripts.put("trigger.trg", triggerGeneratorService.generateFullTriggerSql(schema, tableName));

        return scripts;
    }

    // src/main/java/org/example/money_busters_springboot/service/TriggerService.java

    public Map<String, String> processTriggerRequest(String schema, String table, boolean saveToDb) {
        Map<String, String> scripts = new HashMap<>();

        // 1. Scriptleri üret
        scripts.put("main", triggerGeneratorService.generateMainTableDdl(schema, table));
        scripts.put("his", triggerGeneratorService.generateHisTableDdl(schema, table));
        scripts.put("trigger", triggerGeneratorService.generateFullTriggerSql(schema, table));
        scripts.put("rollback", triggerGeneratorService.generateRollbackDdl(schema, table));

        // 2. Eğer "Database'e Kaydet" seçiliyse çalıştır
        if (saveToDb) {
            if (!triggerRepository.tableExists(schema, table + "_HIS")) {
                triggerRepository.execute(scripts.get("his"));
            }
            triggerRepository.execute(scripts.get("trigger"));
        }

        return scripts;
    }
}
