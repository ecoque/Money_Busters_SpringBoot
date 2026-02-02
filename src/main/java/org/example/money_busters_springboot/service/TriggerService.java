package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TriggerService {

    private final TriggerRepository repository;
    private final TriggerGeneratorService generator;
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.username}")
    private String currentDbUser;

    public TriggerService(TriggerRepository repository, TriggerGeneratorService generator, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.generator = generator;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- CONTROLLER İÇİN EKLENEN EKSİK METODLAR ---
    public void createInsertTrigger(String tableName) {
        String schema = getCurrentSchemaOrThrow();
        processTriggerRequest(schema, tableName, true);
    }

    // BU METODU EKLEMEN GEREKİYOR 👇
    public void createInsertTrigger(String schema, String tableName) {
        processTriggerRequest(schema, tableName, true);
    }

    public Map<String, String> generateAllScripts(String tableName) {
        // ESKİSİ: (currentDbUser != null) ? currentDbUser : "UPT"; ❌

        // YENİSİ: Kullanıcı yoksa HATA ver! ✅
        String schema = getCurrentSchemaOrThrow();
        return processTriggerRequest(schema, tableName, false);
    }

    // --- MEVCUT METODLAR ---
    public List<String> getAllSchemas() { return repository.findAllSchemasFiltered(); }
    public List<String> getTablesBySchema(String schema) { return repository.findTablesWithoutHis(schema); }

    public boolean checkIfAnyExists(String schema, String table) {
        return repository.historyTableExists(schema, table + "_HIS") ||
                repository.triggerExists(schema, "TRG_" + table) ||
                repository.sequenceExists(schema, "SEQ_" + table + "_HIS");
    }

    // --- GÜNCELLENMİŞ VE LOGLAYAN METOT ---
    public Map<String, String> processTriggerRequest(String schema, String table, boolean saveToDb) {
        Map<String, String> s = new HashMap<>();
        String trgName = "TRG_" + table.toUpperCase();
        String hisName = table.toUpperCase() + "_HIS";
        String seqName = "SEQ_" + table.toUpperCase() + "_HIS";

        // KONSOL LOGLARI (Hata Ayıklama İçin)
        boolean hisExists = repository.historyTableExists(schema, hisName);
        boolean trgExists = repository.triggerExists(schema, trgName);
        boolean seqExists = repository.sequenceExists(schema, seqName);

        System.out.println("--- DURUM KONTROLÜ (" + table + ") ---");
        System.out.println("HIS Tablosu Var mı (" + hisName + "): " + hisExists);
        System.out.println("Trigger Var mı (" + trgName + "): " + trgExists);
        System.out.println("Sequence Var mı: " + seqExists);

        // 1. OLUŞTURMA SCRIPTLERI
        s.put("main", generator.generateMainTableDdl(schema, table));
        s.put("seq", generator.generateSequenceDdl(schema, table));
        s.put("trigger", generator.generateFullTriggerSql(schema, table));

        // 2. HIS TABLE ROLLBACK MANTIĞI
        if (hisExists) {
            // Tablo VARSA: Sadece yeni sütunları bul
            List<Map<String, Object>> mainCols = repository.getTableColumns(schema, table);
            List<Map<String, Object>> hisCols = repository.getTableColumns(schema, hisName);
            Set<String> hisColNames = hisCols.stream().map(c -> c.get("COLUMN_NAME").toString().toUpperCase()).collect(Collectors.toSet());

            StringBuilder alterSql = new StringBuilder("-- GÜNCELLEME (Mevcut Tablo Korundu)\n");
            List<String> newColumns = new java.util.ArrayList<>();

            for (Map<String, Object> col : mainCols) {
                if (!hisColNames.contains(col.get("COLUMN_NAME").toString().toUpperCase())) {
                    alterSql.append(generator.generateAlterTableAddColumnSql(schema, hisName, col)).append(";\n");
                    newColumns.add(col.get("COLUMN_NAME").toString());
                }
            }
            s.put("his", alterSql.length() > 35 ? alterSql.toString() : "-- Tablo güncel, eklenecek sütun yok.");

            // RB: Sadece eklenenleri sil
            if (!newColumns.isEmpty()) {
                s.put("rb_his", generator.generateAlterTableDropColumnSql(schema, hisName, newColumns) + ";");
            } else {
                s.put("rb_his", "-- Geri alınacak sütun değişikliği yok (Tablo zaten vardı).");
            }
        } else {
            // Tablo YOKSA: Sıfırdan oluştur ve RB'si DROP olur
            s.put("his", generator.generateHisTableDdl(schema, table));
            // Buraya açıklama ekledik: Neden siliyorum? Çünkü yoktu.
            s.put("rb_his", generator.rbHisTable(schema, table) + "; -- (Tablo işlem öncesinde YOKTU, o yüzden siliniyor)");
        }

        // 3. TRIGGER ROLLBACK (RESTORE) MANTIĞI
        if (trgExists) {
            String oldDDL = repository.getTriggerDDL(schema, trgName);
            if (oldDDL != null) {
                s.put("rb_trg", "-- ESKİ TRIGGER YEDEĞİ (RESTORE)\n" + oldDDL + "\n/");
            } else {
                // Trigger var ama kodunu okuyamadık (Yetki hatası olabilir)
                s.put("rb_trg", "-- UYARI: Trigger vardı ama eski kod okunamadı (Yetki sorunu olabilir).\n" +
                        "DROP TRIGGER " + schema + "." + trgName + ";");
            }
        } else {
            s.put("rb_trg", generator.rbTrigger(schema, table) + "; -- (Trigger işlem öncesinde YOKTU)");
        }

        // 4. SEQUENCE ROLLBACK
        s.put("rb_seq", seqExists ? "-- Sequence zaten vardı, silinmedi." : generator.rbSequence(schema, table) + ";");
        s.put("rb_main", generator.rbMainTable(schema, table) + "; -- (DİKKAT: Ana Tablo Silme Scripti)");

        // --- DB KAYIT ---
        if (saveToDb) {
            if (!hisExists) {
                jdbcTemplate.execute(s.get("his"));
                if (!seqExists) jdbcTemplate.execute(s.get("seq"));
            } else {
                syncHisColumns(schema, table);
            }
            jdbcTemplate.execute(s.get("trigger"));
        }
        return s;
    }

    private void syncHisColumns(String schema, String table) {
        List<Map<String, Object>> mainCols = repository.getTableColumns(schema, table);
        List<Map<String, Object>> hisCols = repository.getTableColumns(schema, table + "_HIS");
        Set<String> hisColNames = hisCols.stream().map(c -> c.get("COLUMN_NAME").toString().toUpperCase()).collect(Collectors.toSet());
        for (Map<String, Object> col : mainCols) {
            if (!hisColNames.contains(col.get("COLUMN_NAME").toString().toUpperCase())) {
                jdbcTemplate.execute(generator.generateAlterTableAddColumnSql(schema, table + "_HIS", col));
            }
        }
    }

    private String getCurrentSchemaOrThrow() {
        if (currentDbUser == null || currentDbUser.trim().isEmpty()) {
            throw new IllegalStateException("HATA: Sisteme giriş yapmış aktif bir veritabanı kullanıcısı bulunamadı!");
        }
        return currentDbUser.toUpperCase();
    }

    public List<TriggerMetadata> getAllTriggers() { return repository.findAllTriggers(); }
    public List<TriggerMetadata> getTriggersByTable(String t) { return repository.findTriggersByTableName(t); }
    public TriggerMetadata getTriggerByName(String n) { return repository.findTriggerByName(n); }
    public void enableTrigger(String n) { repository.setTriggerStatus(n, true); }
    public void disableTrigger(String n) { repository.setTriggerStatus(n, false); }
}