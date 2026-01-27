package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TriggerGeneratorService {
    private final TriggerRepository repository;
    public TriggerGeneratorService(TriggerRepository repository) { this.repository = repository; }

    public String generateMainTableDdl(String schema, String tableName) {
        List<Map<String, Object>> columns = repository.getTableColumns(schema, tableName);
        StringBuilder ddl = new StringBuilder(String.format("CREATE TABLE %s.%s (\n", schema, tableName));
        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> col = columns.get(i);
            ddl.append("  ").append(col.get("COLUMN_NAME")).append(" ").append(col.get("DATA_TYPE"));
            if (col.get("DATA_TYPE").toString().contains("CHAR")) ddl.append("(").append(col.get("DATA_LENGTH")).append(")");
            if (i < columns.size() - 1) ddl.append(",");
            ddl.append("\n");
        }
        return ddl.append(");").toString();
    }

    public String generateHisTableDdl(String schema, String tableName) {
        List<Map<String, Object>> columns = repository.getTableColumns(schema, tableName);
        StringBuilder ddl = new StringBuilder(String.format("CREATE TABLE %s.%s_HIS (\n  HIS_X NUMBER PRIMARY KEY, OP_TYPE CHAR(1), DML_USER VARCHAR2(100), DML_DATE DATE,\n", schema, tableName));
        for (Map<String, Object> col : columns) {
            ddl.append("  ").append(col.get("COLUMN_NAME")).append(" ").append(col.get("DATA_TYPE"));
            if (col.get("DATA_TYPE").toString().contains("CHAR")) ddl.append("(").append(col.get("DATA_LENGTH")).append(")");
            ddl.append(",\n");
        }
        ddl.setLength(ddl.length() - 2);
        return ddl.append("\n);").toString();
    }

    public String generateSequenceDdl(String schema, String tableName) {
        return String.format("CREATE SEQUENCE %s.SEQ_%s_HIS START WITH 1 INCREMENT BY 1;", schema, tableName.toUpperCase());
    }

    public String generateFullTriggerSql(String schema, String tableName) {
        List<Map<String, Object>> columns = repository.getTableColumns(schema, tableName);
        String colList = columns.stream().map(c -> c.get("COLUMN_NAME").toString()).collect(Collectors.joining(", "));
        String newVals = columns.stream().map(c -> ":NEW." + c.get("COLUMN_NAME")).collect(Collectors.joining(", "));
        String oldVals = columns.stream().map(c -> ":OLD." + c.get("COLUMN_NAME")).collect(Collectors.joining(", "));
        return String.format("CREATE OR REPLACE TRIGGER %s.TRG_%s ...", schema, tableName); // SQL detayları burada
    }

    public String generateRollbackDdl(String schema, String tableName) {
        return String.format("DROP TRIGGER %s.TRG_%s;\nDROP TABLE %s.%s_HIS;\nDROP SEQUENCE %s.SEQ_%s_HIS;",
                schema, tableName, schema, tableName, schema, tableName);
    }
}