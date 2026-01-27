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

    /**
     * Sistem şemalarını hariç tutarak tüm şemaları getirir (UI için)
     */
    public List<String> findAllSchemasFiltered() {
        String sql = """
            SELECT DISTINCT OWNER FROM ALL_TABLES 
            WHERE OWNER NOT IN ('SYS', 'SYSTEM', 'DBSNMP', 'OUTLN', 'APPQOSSYS', 
            'DBSFWUSER', 'GGSYS', 'ANONYMOUS', 'CTXSYS', 'DVSYS', 'DVF', 
            'GSMADMIN_INTERNAL', 'MDSYS', 'OLAPSYS', 'ORDDATA', 'ORDSYS', 
            'ORDPLUGINS', 'SI_INFORMTN_SCHEMA', 'WMSYS', 'XDB', 'LBACSYS', 
            'OJVMSYS', 'APEX_PUBLIC_USER', 'APEX_040000', 'FLOWS_FILES') 
            ORDER BY OWNER
            """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Belirli şemadaki tabloları getirir (HIS içermeyenler)
     */
    public List<String> findTablesWithoutHis(String schema) {
        String sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME NOT LIKE '%HIS%' ORDER BY TABLE_NAME";
        return jdbcTemplate.queryForList(sql, String.class, schema.toUpperCase());
    }

    /**
     * History tablosunun var olup olmadığını kontrol eder
     */
    public boolean historyTableExists(String schema, String hisTableName) {
        String sql = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schema.toUpperCase(), hisTableName.toUpperCase());
        return count != null && count > 0;
    }

    /**
     * Nesneyi siler (TRIGGER, TABLE, SEQUENCE) - varsa
     */
    public void dropIfExists(String objectType, String schema, String objectName) {
        try {
            String dropSql = String.format("DROP %s %s.%s", objectType, schema, objectName);
            jdbcTemplate.execute(dropSql);
            System.out.println(objectType + " silindi: " + objectName);
        } catch (Exception e) {
            System.out.println(objectType + " bulunamadı (yeni oluşturulacak): " + objectName);
        }
    }
}
