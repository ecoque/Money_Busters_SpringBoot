package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TriggerGeneratorService {
    private final TriggerRepository triggerRepository;
    public TriggerGeneratorService(TriggerRepository triggerRepository) { this.triggerRepository = triggerRepository; }

    // SADECE TABLO OLUŞTURUR (Noktalı virgül silindi)
    public String generateHisTableDdl(String schema, String tableName) {
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        StringBuilder ddl = new StringBuilder(String.format("CREATE TABLE %s.%s_HIS (\n  HIS_X NUMBER PRIMARY KEY, OP_TYPE CHAR(1), DML_USER VARCHAR2(100), DML_DATE DATE,\n", schema, tableName));
        for (Map<String, Object> col : columns) {
            ddl.append("  ").append(col.get("COLUMN_NAME")).append(" ").append(col.get("DATA_TYPE"));
            if (col.get("DATA_TYPE").toString().contains("CHAR")) ddl.append("(").append(col.get("DATA_LENGTH")).append(")");
            ddl.append(",\n");
        }
        ddl.setLength(ddl.length() - 2);
        return ddl.append("\n)").toString(); // Semicolon sildik
    }

    // AYRI BİR METOT OLARAK SEQUENCE ÜRETİR
    public String generateSequenceDdl(String schema, String tableName) {
        return String.format("CREATE SEQUENCE %s.SEQ_%s_HIS START WITH 1 INCREMENT BY 1", schema, tableName.toUpperCase());
    }

    public String generateFullTriggerSql(String schema, String tableName) {
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        String colList = columns.stream().map(c -> c.get("COLUMN_NAME").toString()).collect(Collectors.joining(", "));
        String nV = columns.stream().map(c -> ":NEW." + c.get("COLUMN_NAME")).collect(Collectors.joining(", "));
        String oV = columns.stream().map(c -> ":OLD." + c.get("COLUMN_NAME")).collect(Collectors.joining(", "));

        return String.format("""
            CREATE OR REPLACE TRIGGER %s.TRG_%s
            AFTER INSERT OR UPDATE OR DELETE ON %s.%s
            FOR EACH ROW
            DECLARE v_op_type CHAR(1);
            BEGIN
                IF INSERTING THEN v_op_type := 'I';
                    INSERT INTO %s.%s_HIS (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.SEQ_%s_HIS.NEXTVAL, v_op_type, USER, SYSDATE, %s);
                ELSIF UPDATING THEN v_op_type := 'U';
                    INSERT INTO %s.%s_HIS (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.SEQ_%s_HIS.NEXTVAL, v_op_type, USER, SYSDATE, %s);
                ELSIF DELETING THEN v_op_type := 'D';
                    INSERT INTO %s.%s_HIS (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.SEQ_%s_HIS.NEXTVAL, v_op_type, USER, SYSDATE, %s);
                END IF;
            END;""", schema, tableName, schema, tableName, schema, tableName, colList, schema, tableName, nV,
                schema, tableName, colList, schema, tableName, nV, schema, tableName, colList, schema, tableName, oV);
    }

    public String generateRollbackDdl(String schema, String tableName) {
        return String.format("DROP TRIGGER %s.TRG_%s", schema, tableName); // Basit hali
    }
}