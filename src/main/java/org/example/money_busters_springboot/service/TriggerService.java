package org.example.money_busters_springboot.service;

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
       ŞEMA VE TABLO LİSTELEME
       =========================== */

    /**
     * Tüm şemaları listeler (sistem şemaları hariç)
     */
    public List<String> getAllSchemas() {
        return triggerRepository.findAllSchemasFiltered();
    }

    /**
     * Belirtilen şemadaki tabloları listeler (HIS hariç)
     */
    public List<String> getTablesBySchema(String schema) {
        return triggerRepository.findTablesWithoutHis(schema);
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
       HIS TABLO KONTROLÜ
       =========================== */

    /**
     * History tablosunun var olup olmadığını kontrol eder
     */
    public boolean checkHistoryTableExists(String schema, String tableName) {
        String hisTableName = tableName.toUpperCase() + "_HIS";
        return triggerRepository.historyTableExists(schema, hisTableName);
    }

    /* ===========================
       TRIGGER OLUŞTURMA
       =========================== */

    /**
     * Sadece trigger'ı yeniden oluşturur (HIS tablosuna dokunmaz)
     */
    public void recreateTriggerOnly(String schema, String tableName) {
        String triggerName = "TRG_" + tableName.toUpperCase();
        
        // Önce varsa trigger'ı sil
        triggerRepository.dropIfExists("TRIGGER", schema, triggerName);
        
        // Trigger'ı yeniden oluştur
        String triggerSql = triggerGeneratorService.generateFullTriggerSql(schema, tableName);
        jdbcTemplate.execute(triggerSql);
        System.out.println("Trigger yeniden oluşturuldu: " + triggerName);
    }

    /**
     * Trigger, History tablosu ve Sequence oluşturur
     */
    public void createTriggerWithHistoryTable(String schema, String tableName) {
        String triggerName = "TRG_" + tableName.toUpperCase();
        String hisTableName = tableName.toUpperCase() + "_HIS";
        String seqName = "SEQ_" + hisTableName;

        // 1. Varsa önce sil (sıra önemli: trigger -> tablo -> sequence)
        triggerRepository.dropIfExists("TRIGGER", schema, triggerName);
        triggerRepository.dropIfExists("TABLE", schema, hisTableName);
        triggerRepository.dropIfExists("SEQUENCE", schema, seqName);

        // 2. Tablo kolonlarını al
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        if (columns.isEmpty()) {
            throw new RuntimeException("Tablo kolonları bulunamadı: " + tableName);
        }

        // 3. History tablosu oluştur
        String hisTableDdl = triggerGeneratorService.generateHisTableDdl(schema, tableName);
        System.out.println("History Table DDL:\n" + hisTableDdl);
        jdbcTemplate.execute(hisTableDdl);
        System.out.println("History tablosu oluşturuldu: " + hisTableName);

        // 4. Sequence oluştur
        String seqDdl = triggerGeneratorService.generateSequenceDdl(schema, tableName);
        System.out.println("Sequence DDL:\n" + seqDdl);
        jdbcTemplate.execute(seqDdl);
        System.out.println("Sequence oluşturuldu: " + seqName);

        // 5. Trigger oluştur
        String triggerSql = triggerGeneratorService.generateFullTriggerSql(schema, tableName);
        System.out.println("Trigger SQL:\n" + triggerSql);
        jdbcTemplate.execute(triggerSql);
        System.out.println("Trigger oluşturuldu: " + triggerName);
    }

    public void createInsertTrigger(String schema, String tableName) {
        String sql = triggerGeneratorService.generateFullTriggerSql(schema, tableName);
        jdbcTemplate.execute(sql);
    }

    /* ===========================
       SCRIPT ÜRETİMİ
       =========================== */

    /**
     * Ana tablo DDL scriptini döner
     */
    public String generateMainTableDdl(String schema, String tableName) {
        return triggerGeneratorService.generateMainTableDdl(schema, tableName);
    }

    /**
     * HIS tablosu DDL scriptini döner
     */
    public String generateHisTableDdl(String schema, String tableName) {
        return triggerGeneratorService.generateHisTableDdl(schema, tableName);
    }

    /**
     * Trigger DDL scriptini döner
     */
    public String generateTriggerDdl(String schema, String tableName) {
        return triggerGeneratorService.generateFullTriggerSql(schema, tableName);
    }

    /**
     * Sequence DDL scriptini döner
     */
    public String generateSequenceDdl(String schema, String tableName) {
        return triggerGeneratorService.generateSequenceDdl(schema, tableName);
    }

    public Map<String, String> generateAllScripts(String tableName) {
        String schema = "UPT";
        Map<String, String> scripts = new HashMap<>();

        scripts.put("main.ddl", triggerGeneratorService.generateMainTableDdl(schema, tableName));
        scripts.put("rollback[RB].ddl", triggerGeneratorService.generateRollbackDdl(schema, tableName));
        scripts.put("trigger.trg", triggerGeneratorService.generateFullTriggerSql(schema, tableName));

        return scripts;
    }
}
