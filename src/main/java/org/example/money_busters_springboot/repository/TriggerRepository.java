package org.example.money_busters_springboot.repository;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
}
