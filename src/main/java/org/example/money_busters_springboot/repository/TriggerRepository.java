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

    public List<String> findAllSchemas() {
        return jdbcTemplate.queryForList("SELECT DISTINCT owner FROM all_tables " +
                "WHERE owner NOT IN ('SYS', 'SYSTEM', 'DBSNMP', 'OUTLN') ORDER BY owner", String.class);
    }

    public List<String> findTablesBySchema(String schemaName) {
        String sql = "SELECT table_name FROM all_tables " +
                "WHERE owner = ? AND table_name NOT LIKE '%_HIS' ORDER BY table_name";
        return jdbcTemplate.queryForList(sql, String.class, schemaName.toUpperCase());
    }

    public List<Map<String, Object>> getTableColumns(String schemaName, String tableName) {
        String sql = "SELECT column_name, data_type, data_length, data_precision, data_scale " +
                "FROM all_tab_columns WHERE owner = ? AND table_name = ? ORDER BY column_id";
        return jdbcTemplate.queryForList(sql, schemaName.toUpperCase(), tableName.toUpperCase());
    }

    public boolean tableExists(String schema, String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM all_tables WHERE owner = ? AND table_name = ?",
                Integer.class, schema.toUpperCase(), tableName.toUpperCase());
        return count != null && count > 0;
    }

    // Controller hatalarını çözen metodlar
    public List<TriggerMetadata> findAllTriggers() {
        return jdbcTemplate.query("SELECT * FROM USER_TRIGGERS", new TriggerRowMapper());
    }
    public List<TriggerMetadata> findTriggersByTableName(String table) {
        return jdbcTemplate.query("SELECT * FROM USER_TRIGGERS WHERE TABLE_NAME = ?", new TriggerRowMapper(), table.toUpperCase());
    }
    public TriggerMetadata findTriggerByName(String name) {
        List<TriggerMetadata> list = jdbcTemplate.query("SELECT * FROM USER_TRIGGERS WHERE TRIGGER_NAME = ?", new TriggerRowMapper(), name.toUpperCase());
        return list.isEmpty() ? null : list.get(0);
    }
    public void setTriggerStatus(String name, boolean enable) {
        jdbcTemplate.execute("ALTER TRIGGER " + name + (enable ? " ENABLE" : " DISABLE"));
    }

    private static class TriggerRowMapper implements RowMapper<TriggerMetadata> {
        @Override
        public TriggerMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TriggerMetadata(rs.getString("TRIGGER_NAME"), rs.getString("TABLE_NAME"), "", "", rs.getString("STATUS"), "");
        }
    }
}