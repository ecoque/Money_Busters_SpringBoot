package org.example.money_busters_springboot.dto;

import java.util.List;
import java.util.Map;

/**
 * Generic tablo verisi DTO
 * Herhangi bir tablo için kullanılabilir
 */
public class GenericTableDataDTO {
    private String tableName;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private int totalRows;

    public GenericTableDataDTO(String tableName, List<String> columns,
                               List<Map<String, Object>> rows) {
        this.tableName = tableName;
        this.columns = columns;
        this.rows = rows;
        this.totalRows = rows.size();
    }

    // Getter ve Setter'lar
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
        this.totalRows = rows.size();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
}
