package org.example.money_busters_springboot.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class DatabaseConnectionService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean testConnection() {
        try {
            String result = jdbcTemplate.queryForObject("SELECT 'CONNECTION_OK' FROM DUAL", String.class);
            return "CONNECTION_OK".equals(result);
        } catch (Exception e) {
            throw new RuntimeException("Database bağlantısı başarısız: " + e.getMessage(), e);
        }
    }

    public String getCurrentUser() {
        return jdbcTemplate.queryForObject("SELECT USER FROM DUAL", String.class);
    }

    public String getDatabaseVersion() {
        return jdbcTemplate.queryForObject("SELECT BANNER FROM V$VERSION WHERE ROWNUM = 1", String.class);
    }
}
