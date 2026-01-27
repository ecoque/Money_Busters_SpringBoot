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

    // Controller'ın beklediği tüm metodlar:
    public List<TriggerMetadata> getAllTriggers() { return repository.findAllTriggers(); }
    public List<TriggerMetadata> getTriggersByTable(String t) { return repository.findTriggersByTableName(t); }
    public TriggerMetadata getTriggerByName(String n) { return repository.findTriggerByName(n); }
    public void enableTrigger(String n) { repository.setTriggerStatus(n, true); }
    public void disableTrigger(String n) { repository.setTriggerStatus(n, false); }

    public void createInsertTrigger(String s, String t) { repository.execute(generator.generateFullTriggerSql(s, t)); }
    public Map<String, String> generateAllScripts(String t) { return processTriggerRequest("UPT", t, false); }

    // DATABASE'E KAYDETME MANTIĞI (Hata almayan hali)
    public Map<String, String> processTriggerRequest(String schema, String table, boolean saveToDb) {
        Map<String, String> scripts = new HashMap<>();
        scripts.put("his", generator.generateHisTableDdl(schema, table));
        scripts.put("seq", generator.generateSequenceDdl(schema, table));
        scripts.put("trigger", generator.generateFullTriggerSql(schema, table));

        if (saveToDb) {
            // Komutları ayrı ayrı çalıştırıyoruz (JDBC kuralı)
            if (!repository.tableExists(schema, table + "_HIS")) {
                repository.execute(scripts.get("his"));
                repository.execute(scripts.get("seq"));
            }
            repository.execute(scripts.get("trigger"));
        }
        return scripts;
    }
}