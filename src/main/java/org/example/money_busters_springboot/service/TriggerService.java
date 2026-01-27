package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TriggerService {
    private final TriggerRepository repository;
    private final TriggerGeneratorService generator;

    public TriggerService(TriggerRepository repository, TriggerGeneratorService generator) {
        this.repository = repository;
        this.generator = generator;
    }

    // Controller'ın aradığı tüm metodlar burada:
    public List<TriggerMetadata> getAllTriggers() { return repository.findAllTriggers(); }
    public List<TriggerMetadata> getTriggersByTable(String table) { return repository.findTriggersByTableName(table); }
    public TriggerMetadata getTriggerByName(String name) { return repository.findTriggerByName(name); }
    public void enableTrigger(String name) { repository.setTriggerStatus(name, true); }
    public void disableTrigger(String name) { repository.setTriggerStatus(name, false); }

    public void createInsertTrigger(String schema, String tableName) {
        repository.execute(generator.generateFullTriggerSql(schema, tableName));
    }

    public Map<String, String> generateAllScripts(String tableName) {
        return processTriggerRequest("UPT", tableName, false);
    }

    // UI'daki RadioButton seçimine göre iş yapan ana metod
    public Map<String, String> processTriggerRequest(String schema, String table, boolean saveToDb) {
        Map<String, String> scripts = new HashMap<>();
        scripts.put("main", generator.generateMainTableDdl(schema, table));
        scripts.put("his", generator.generateHisTableDdl(schema, table));
        scripts.put("trigger", generator.generateFullTriggerSql(schema, table));
        scripts.put("seq", generator.generateSequenceDdl(schema, table));
        scripts.put("rollback", generator.generateRollbackDdl(schema, table));

        if (saveToDb) {
            if (!repository.tableExists(schema, table + "_HIS")) {
                repository.execute(scripts.get("his"));
                repository.execute(scripts.get("seq"));
            }
            repository.execute(scripts.get("trigger"));
        }
        return scripts;
    }
}