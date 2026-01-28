package org.example.money_busters_springboot.repository;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Trigger işlemleri için repository sınıfı
 */
@Repository
public class TriggerRepository {

    private final JdbcTemplate jdbcTemplate;

    public TriggerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Tüm trigger'ları listeler
     */
    public List<TriggerMetadata> findAllTriggers() {
        String sql = """
            SELECT TRIGGER_NAME, TABLE_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, STATUS, TRIGGER_BODY
            FROM USER_TRIGGERS
            ORDER BY TRIGGER_NAME
            """;

        return jdbcTemplate.query(sql, new TriggerRowMapper());
    }
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }


    // Oracle'daki giriş yapmış kullanıcıya ait tüm tabloları getirir
    public List<String> findAllTables() {
        String sql = "SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    /**
     * Belirli bir tabloya ait trigger'ları listeler
     */
    public List<TriggerMetadata> findTriggersByTableName(String tableName) {
        String sql = """
            SELECT TRIGGER_NAME, TABLE_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, STATUS, TRIGGER_BODY
            FROM USER_TRIGGERS
            WHERE TABLE_NAME = ?
            ORDER BY TRIGGER_NAME
            """;

        return jdbcTemplate.query(sql, new TriggerRowMapper(), tableName.toUpperCase());
    }

    /**
     * Trigger adına göre trigger bilgisini getirir
     */
    public TriggerMetadata findTriggerByName(String triggerName) {
        String sql = """
            SELECT TRIGGER_NAME, TABLE_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, STATUS, TRIGGER_BODY
            FROM USER_TRIGGERS
            WHERE TRIGGER_NAME = ?
            """;

        List<TriggerMetadata> results = jdbcTemplate.query(sql, new TriggerRowMapper(), triggerName.toUpperCase());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Trigger'ı aktif/pasif yapar
     */
    public void setTriggerStatus(String triggerName, boolean enable) {
        String action = enable ? "ENABLE" : "DISABLE";
        String sql = "ALTER TRIGGER " + triggerName + " " + action;
        jdbcTemplate.execute(sql);
    }

    /**
     * RowMapper sınıfı
     */
    private static class TriggerRowMapper implements RowMapper<TriggerMetadata> {
        @Override
        public TriggerMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TriggerMetadata(
                rs.getString("TRIGGER_NAME"),
                rs.getString("TABLE_NAME"),
                rs.getString("TRIGGER_TYPE"),
                rs.getString("TRIGGERING_EVENT"),
                rs.getString("STATUS"),
                rs.getString("TRIGGER_BODY")
            );
        }
    }
    // Tüm şemaları getirir
    public List<String> findAllSchemas() {
        String sql = "SELECT DISTINCT owner FROM all_tables ORDER BY owner";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // Seçilen şemaya ait tabloları getirir
    public List<String> findTablesBySchema(String schemaName) {
        String sql = "SELECT table_name FROM all_tables WHERE owner = ? ORDER BY table_name";
        return jdbcTemplate.queryForList(sql, String.class, schemaName.toUpperCase());
    }

    // Tablonun kolon bilgilerini getirir (HIS tablosunu oluşturmak için kritik)
    public List<Map<String, Object>> getTableColumns(String schemaName, String tableName) {
        String sql = """
        SELECT column_name, data_type, data_length, data_precision, data_scale, nullable 
        FROM all_tab_columns 
        WHERE owner = ? AND table_name = ? 
        ORDER BY column_id
        """;
        return jdbcTemplate.queryForList(sql, schemaName.toUpperCase(), tableName.toUpperCase());
    }

    // --- BURADAN AŞAĞISINI EKLE ---

    // 1. Temiz Şema Listesi (Sadece GRANT yetkisi olanlar)
    public List<String> findAllSchemasFiltered() {
        // Kullanıcının yetkili olduğu şemaları getir (sistem şemalarını hariç tut)
        String sql = """
            SELECT DISTINCT owner 
            FROM all_tables 
            WHERE owner NOT IN ('SYS', 'SYSTEM', 'DBSNMP', 'OUTLN', 'XDB', 'WMSYS', 
                                'APEX_040000', 'MDSYS', 'CTXSYS', 'ORDSYS', 'EXFSYS',
                                'ORDDATA', 'SYSMAN', 'OLAPSYS', 'FLOWS_FILES', 'APPQOSSYS')
            AND owner IN (
                -- Kullanıcının kendi şeması
                SELECT username FROM user_users
                UNION
                -- INSERT yetkisi olanlar
                SELECT table_schema FROM all_tab_privs 
                WHERE grantee = USER AND privilege = 'INSERT'
                UNION
                -- Rol üzerinden alınan yetkiler
                SELECT table_schema FROM all_tab_privs 
                WHERE grantee IN (SELECT granted_role FROM user_role_privs) 
                AND privilege = 'INSERT'
            )
            ORDER BY owner
            """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 2. Sadece Ana Tabloları Getir (_HIS olmayanlar)
    public List<String> findTablesWithoutHis(String schema) {
        String sql = "SELECT table_name FROM all_tables WHERE owner = ? AND table_name NOT LIKE '%_HIS' ORDER BY table_name";
        return jdbcTemplate.queryForList(sql, String.class, schema.toUpperCase());
    }

    // 3. HIS Tablosu Var mı Kontrolü
    public boolean historyTableExists(String schema, String tableName) {
        String sql = "SELECT count(*) FROM all_tables WHERE owner = ? AND table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schema.toUpperCase(), tableName.toUpperCase());
        return count != null && count > 0;
    }

    // 4. Trigger Var mı Kontrolü
    public boolean triggerExists(String schema, String triggerName) {
        String sql = "SELECT count(*) FROM all_triggers WHERE owner = ? AND trigger_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schema.toUpperCase(), triggerName.toUpperCase());
        return count != null && count > 0;
    }

    // 5. Sequence Var mı Kontrolü
    public boolean sequenceExists(String schema, String seqName) {
        String sql = "SELECT count(*) FROM all_sequences WHERE sequence_owner = ? AND sequence_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schema.toUpperCase(), seqName.toUpperCase());
        return count != null && count > 0;
    }

    // 6. Eski Trigger Kodunu Çekme (Rollback İçin Restore Özelliği)
    public String getTriggerDDL(String schema, String triggerName) {
        try {
            String sql = "SELECT DBMS_METADATA.GET_DDL('TRIGGER', ?, ?) FROM DUAL";
            return jdbcTemplate.queryForObject(sql, String.class, triggerName.toUpperCase(), schema.toUpperCase());
        } catch (Exception e) {
            // Hata olursa konsola yazsın, biz de görelim
            System.err.println("Eski Trigger DDL okunamadı: " + triggerName + " -> " + e.getMessage());
            return null;
        }
    }
}
