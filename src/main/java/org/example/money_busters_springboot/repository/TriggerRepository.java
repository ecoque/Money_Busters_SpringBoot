package org.example.money_busters_springboot.repository;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class TriggerRepository {
    private final JdbcTemplate jdbcTemplate;
    public TriggerRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public void execute(String sql) { jdbcTemplate.execute(sql); }

    public List<TriggerMetadata> findAllTriggers() {
        return jdbcTemplate.query("SELECT * FROM USER_TRIGGERS", new TriggerRowMapper());
    }

    public List<TriggerMetadata> findTriggersByTableName(String t) {
        return jdbcTemplate.query("SELECT * FROM USER_TRIGGERS WHERE TABLE_NAME = ?", new TriggerRowMapper(), t.toUpperCase());
    }

    public TriggerMetadata findTriggerByName(String n) {
        List<TriggerMetadata> results = jdbcTemplate.query("SELECT * FROM USER_TRIGGERS WHERE TRIGGER_NAME = ?", new TriggerRowMapper(), n.toUpperCase());
        return results.isEmpty() ? null : results.get(0);
    }

    public void setTriggerStatus(String n, boolean e) {
        jdbcTemplate.execute("ALTER TRIGGER " + n + (e ? " ENABLE" : " DISABLE"));
    }

    public List<String> findAllSchemas() {
        return jdbcTemplate.queryForList("SELECT DISTINCT owner FROM all_tables ORDER BY owner", String.class);
    }

    public List<String> findTablesBySchema(String s) {
        return jdbcTemplate.queryForList("SELECT table_name FROM all_tables WHERE owner = ? AND table_name NOT LIKE '%_HIS' ORDER BY table_name", String.class, s.toUpperCase());
    }

    public boolean tableExists(String s, String t) {
        Integer c = jdbcTemplate.queryForObject("SELECT count(*) FROM all_tables WHERE owner = ? AND table_name = ?", Integer.class, s.toUpperCase(), t.toUpperCase());
        return c != null && c > 0;
    }

    public List<Map<String, Object>> getTableColumns(String s, String t) {
        return jdbcTemplate.queryForList("SELECT column_name, data_type, data_length, data_precision, data_scale FROM all_tab_columns WHERE owner = ? AND table_name = ?", s.toUpperCase(), t.toUpperCase());
    }

    private static class TriggerRowMapper implements RowMapper<TriggerMetadata> {
        @Override
        public TriggerMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TriggerMetadata(rs.getString("TRIGGER_NAME"), rs.getString("TABLE_NAME"), "", "", rs.getString("STATUS"), "");
        }
    }
}