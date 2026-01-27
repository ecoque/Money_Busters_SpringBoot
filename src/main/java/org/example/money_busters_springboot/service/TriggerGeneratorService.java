package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.repository.TriggerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TriggerGeneratorService {

    private final TriggerRepository triggerRepository;

    public TriggerGeneratorService(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }

    // --- EKSİK OLAN METODLAR EKLENDİ ---

    // 1. Ana Tablo DDL (Controller bunu arıyordu)
    public String generateMainTableDdl(String schema, String tableName) {
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
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

    // 2. Sequence DDL (Controller bunu arıyordu)
    public String generateSequenceDdl(String schema, String tableName) {
        return String.format("CREATE SEQUENCE %s.SEQ_%s_HIS START WITH 1 INCREMENT BY 1", schema, tableName.toUpperCase());
    }

    // --- MEVCUT METODLAR ---

    public String generateAlterTableAddColumnSql(String schema, String tableName, Map<String, Object> col) {
        String colName = col.get("COLUMN_NAME").toString();
        String dataType = col.get("DATA_TYPE").toString();
        Object dataLen = col.get("DATA_LENGTH");
        StringBuilder sql = new StringBuilder(String.format("ALTER TABLE %s.%s ADD %s ", schema, tableName, colName));
        if (dataType.contains("CHAR")) sql.append(dataType).append("(").append(dataLen).append(")");
        else sql.append(dataType);
        return sql.toString();
    }

    public String generateAlterTableDropColumnSql(String schema, String tableName, List<String> columnsToDrop) {
        if (columnsToDrop == null || columnsToDrop.isEmpty()) return "";
        StringBuilder sql = new StringBuilder(String.format("ALTER TABLE %s.%s DROP (", schema, tableName));
        for (int i = 0; i < columnsToDrop.size(); i++) {
            sql.append(columnsToDrop.get(i));
            if (i < columnsToDrop.size() - 1) sql.append(", ");
        }
        return sql.append(")").toString();
    }

    public String rbTrigger(String s, String t) { return "DROP TRIGGER " + s + ".TRG_" + t.toUpperCase(); }
    public String rbHisTable(String s, String t) { return "DROP TABLE " + s + "." + t.toUpperCase() + "_HIS"; }
    public String rbSequence(String s, String t) { return "DROP SEQUENCE " + s + ".SEQ_" + t.toUpperCase() + "_HIS"; }
    public String rbMainTable(String s, String t) { return "DROP TABLE " + s + "." + t.toUpperCase(); }

    public String generateHisTableDdl(String schema, String tableName) {
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        StringBuilder ddl = new StringBuilder(String.format("CREATE TABLE %s.%s_HIS (\n  HIS_X NUMBER PRIMARY KEY, OP_TYPE CHAR(1), DML_USER VARCHAR2(100), DML_DATE DATE,\n", schema, tableName));
        for (Map<String, Object> col : columns) {
            ddl.append("  ").append(col.get("COLUMN_NAME")).append(" ").append(col.get("DATA_TYPE"));
            if (col.get("DATA_TYPE").toString().contains("CHAR")) ddl.append("(").append(col.get("DATA_LENGTH")).append(")");
            ddl.append(",\n");
        }
        ddl.setLength(ddl.length() - 2);
        return ddl.append("\n)").toString();
    }

    public String generateFullTriggerSql(String schema, String tableName) {
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        String colList = columns.stream().map(c -> c.get("COLUMN_NAME").toString()).collect(Collectors.joining(", "));
        String nV = columns.stream().map(c -> ":NEW." + c.get("COLUMN_NAME")).collect(Collectors.joining(", "));
        String oV = columns.stream().map(c -> ":OLD." + c.get("COLUMN_NAME")).collect(Collectors.joining(", "));
        String trgName = "TRG_" + tableName.toUpperCase();
        String seqName = "SEQ_" + tableName.toUpperCase() + "_HIS";
        String hisTable = tableName.toUpperCase() + "_HIS";

        return String.format("""
            CREATE OR REPLACE TRIGGER %s.%s
            AFTER INSERT OR UPDATE OR DELETE ON %s.%s
            FOR EACH ROW
            DECLARE v_op_type CHAR(1);
            BEGIN
                IF INSERTING THEN v_op_type := 'I';
                    INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.%s.NEXTVAL, v_op_type, USER, SYSDATE, %s);
                ELSIF UPDATING THEN v_op_type := 'U';
                    INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.%s.NEXTVAL, v_op_type, USER, SYSDATE, %s);
                ELSIF DELETING THEN v_op_type := 'D';
                    INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.%s.NEXTVAL, v_op_type, USER, SYSDATE, %s);
                END IF;
            END;""", schema, trgName, schema, tableName, schema, hisTable, colList, schema, seqName, nV,
                schema, hisTable, colList, schema, seqName, nV, schema, hisTable, colList, schema, seqName, oV);
    }
}