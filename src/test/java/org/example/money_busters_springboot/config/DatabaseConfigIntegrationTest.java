package org.example.money_busters_springboot.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class DatabaseConfigIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDataSourceNotNull() {
        assertNotNull(dataSource, "DataSource bean oluşturulmalı");
    }

    @Test
    void testJdbcTemplateNotNull() {
        assertNotNull(jdbcTemplate, "JdbcTemplate bean oluşturulmalı");
    }

    @Test
    void testDatabaseConnection() {

        assertDoesNotThrow(() -> {
            String result = jdbcTemplate.queryForObject(
                    "SELECT 'CONNECTION_OK' FROM DUAL", 
                    String.class
            );
            assertEquals("CONNECTION_OK", result);
        }, "Veritabanı bağlantısı başarılı olmalı");
    }

    @Test
    void testDatabaseVersion() {
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject(
                    "SELECT banner FROM v$version WHERE ROWNUM = 1",
                    String.class
            );
            assertNotNull(version, "Oracle versiyonu alınabilmeli");
            assertTrue(version.contains("Oracle"), "Oracle veritabanı olmalı");
        });
    }
}
