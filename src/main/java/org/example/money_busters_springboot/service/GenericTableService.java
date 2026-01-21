package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.dto.GenericTableDataDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic tablo servisi
 * Herhangi bir tablodan veri çekebilir
 */
@Service
public class GenericTableService {

    private final JdbcTemplate jdbcTemplate;

    public GenericTableService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Herhangi bir tablodan veri çeker
     * @param tableName Tablo adı
     * @return GenericTableDataDTO
     */
    public GenericTableDataDTO getTableData(String tableName) {
        // SQL Injection önlemi için tablo adını kontrol et
        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("Geçersiz tablo adı: " + tableName);
        }

        String sql = "SELECT * FROM " + tableName;

        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            int columnCount = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }

            return row;
        });

        // Sütun isimlerini al
        List<String> columns = new ArrayList<>();
        if (!rows.isEmpty()) {
            columns.addAll(rows.get(0).keySet());
        } else {
            // Tablo boşsa metadata'dan sütun isimlerini al
            columns = getColumnsFromMetadata(tableName);
        }

        return new GenericTableDataDTO(tableName, columns, rows);
    }

    /**
     * Tablo adı güvenlik kontrolü (sadece harf, rakam ve alt çizgi)
     */
    private boolean isValidTableName(String tableName) {
        return tableName != null && tableName.matches("^[A-Za-z0-9_]+$");
    }

    /**
     * Belirli bir kullanıcıya ait tabloları listele
     * @param schemaName Schema adı (kullanıcı adı)
     * @return Tablo isimleri listesi
     */
    public List<String> getUserTables(String schemaName) {
        String sql = "SELECT table_name FROM all_tables WHERE owner = ? ORDER BY table_name";
        return jdbcTemplate.queryForList(sql, String.class, schemaName.toUpperCase());
    }

    /**
     * Tablo boşsa metadata'dan sütun isimlerini alır
     */
    private List<String> getColumnsFromMetadata(String tableName) {
        List<String> columns = new ArrayList<>();
        String sql = "SELECT column_name FROM all_tab_columns WHERE table_name = ? ORDER BY column_id";

        try {
            columns = jdbcTemplate.queryForList(sql, String.class, tableName.toUpperCase());
        } catch (Exception e) {
            // Metadata alınamazsa boş liste dön
        }

        return columns;
    }

    /**
     * Tablodaki kayıt sayısını döner
     */
    public long getTableRowCount(String tableName) {
        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("Geçersiz tablo adı: " + tableName);
        }

        String sql = "SELECT COUNT(*) FROM " + tableName;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }
}
