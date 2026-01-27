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

    /**
     * INSERT, UPDATE ve DELETE işlemlerini tek bir trigger içinde yöneten SQL üretir.
     */
    // TriggerGeneratorService.java içindeki ilgili metodu bununla değiştirin

    public String generateFullTriggerSql(String schema, String tableName) {
        String triggerName = "TRG_" + tableName.toUpperCase();
        String hisTable = tableName.toUpperCase() + "_HIS";
        String sequenceName = "SEQ_" + tableName.toUpperCase() + "_HIS";

        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        List<String> colNames = columns.stream()
                .map(c -> c.get("COLUMN_NAME").toString())
                .collect(Collectors.toList());

        String colList = String.join(", ", colNames);

        // Değer listelerini oluşturma
        String oldValues = colNames.stream().map(c -> ":OLD." + c).collect(Collectors.joining(", "));
        String newValues = colNames.stream().map(c -> ":NEW." + c).collect(Collectors.joining(", "));

        return String.format("""
    CREATE OR REPLACE TRIGGER %s.%s
    AFTER INSERT OR UPDATE OR DELETE ON %s.%s
    FOR EACH ROW
    DECLARE
        v_op_type CHAR(1);
    BEGIN
        IF INSERTING THEN 
            v_op_type := 'I';
            INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
            VALUES (%s.%s.NEXTVAL, v_op_type, SYS_CONTEXT('USERENV','SESSION_USER'), SYSDATE, %s);
            
        ELSIF UPDATING THEN 
            v_op_type := 'U';
            INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
            VALUES (%s.%s.NEXTVAL, v_op_type, SYS_CONTEXT('USERENV','SESSION_USER'), SYSDATE, %s);
            
        ELSIF DELETING THEN 
            v_op_type := 'D';
            INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
            VALUES (%s.%s.NEXTVAL, v_op_type, SYS_CONTEXT('USERENV','SESSION_USER'), SYSDATE, %s);
        END IF;
    END;
    """,
                schema, triggerName, schema, tableName,
                schema, hisTable, colList, schema, sequenceName, newValues, // Insert durumu
                schema, hisTable, colList, schema, sequenceName, newValues, // Update durumu
                schema, hisTable, colList, schema, sequenceName, oldValues  // Delete durumu
        );
    }

    /**
     * History tablosunu (HIS) ve Sequence'i oluşturan DDL scriptini üretir.
     * VARCHAR2 uzunluklarını ve NUMBER hassasiyetlerini otomatik ekler.
     */
    public String generateHisTableDdl(String schema, String tableName) {
        String hisTable = tableName.toUpperCase() + "_HIS";
        String seqName = "SEQ_" + hisTable;
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);

        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format("CREATE TABLE %s.%s (\n", schema, hisTable));
        ddl.append("  HIS_X NUMBER PRIMARY KEY,\n");
        ddl.append("  OP_TYPE CHAR(1),\n");
        ddl.append("  DML_USER VARCHAR2(100),\n");
        ddl.append("  DML_DATE DATE,\n");

        for (Map<String, Object> col : columns) {
            String colName = col.get("COLUMN_NAME").toString();
            String dataType = col.get("DATA_TYPE").toString();
            Object dataLength = col.get("DATA_LENGTH");
            Object precision = col.get("DATA_PRECISION");
            Object scale = col.get("DATA_SCALE");

            ddl.append("  ").append(colName).append(" ");

            // Oracle'ın VARCHAR2/CHAR için parantez içinde uzunluk isteme kuralı
            if (dataType.contains("VARCHAR2") || dataType.contains("CHAR")) {
                ddl.append(dataType).append("(").append(dataLength).append(")");
            }
            // NUMBER tipi için hassasiyet (precision/scale) varsa ekle
            else if (dataType.equals("NUMBER") && precision != null) {
                if (scale != null) {
                    ddl.append("NUMBER(").append(precision).append(",").append(scale).append(")");
                } else {
                    ddl.append("NUMBER(").append(precision).append(")");
                }
            }
            // DATE, TIMESTAMP veya direkt NUMBER
            else {
                ddl.append(dataType);
            }
            ddl.append(",\n");
        }

        ddl.setLength(ddl.length() - 2); // Son virgülü ve alt satırı kaldır
        ddl.append("\n);\n\n");

        ddl.append(String.format("CREATE SEQUENCE %s.%s START WITH 1 INCREMENT BY 1;", schema, seqName));
        return ddl.toString();
    }

    /**
     * Trigger, Tablo ve Sequence'i silmek için Rollback scripti üretir.
     */
    public String generateRollbackDdl(String schema, String tableName) {
        String hisTable = tableName.toUpperCase() + "_HIS";
        String seqName = "SEQ_" + hisTable;
        String triggerName = "TRG_" + tableName.toUpperCase();

        return String.format("""
        DROP TRIGGER %s.%s;
        DROP TABLE %s.%s;
        DROP SEQUENCE %s.%s;
        """, schema, triggerName, schema, hisTable, schema, seqName);
    }

    /**
     * Sequence DDL oluşturur
     */
    public String generateSequenceDdl(String schema, String tableName) {
        String seqName = "SEQ_" + tableName.toUpperCase() + "_HIS";
        return String.format("CREATE SEQUENCE %s.%s START WITH 1 INCREMENT BY 1", schema, seqName);
    }

    /**
     * Ana tablo için DDL oluşturur (CREATE TABLE)
     */
    public String generateMainTableDdl(String schema, String tableName) {
        List<Map<String, Object>> columns = triggerRepository.getTableColumns(schema, tableName);
        
        StringBuilder ddl = new StringBuilder();
        ddl.append("-- ").append(tableName).append(" Ana Tablo DDL\n");
        ddl.append("-- Oluşturulma Tarihi: ").append(java.time.LocalDateTime.now()).append("\n\n");
        ddl.append(String.format("CREATE TABLE %s.%s (\n", schema, tableName));
        
        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> col = columns.get(i);
            String colName = col.get("COLUMN_NAME").toString();
            String dataType = col.get("DATA_TYPE").toString();
            Object dataLength = col.get("DATA_LENGTH");
            Object precision = col.get("DATA_PRECISION");
            Object scale = col.get("DATA_SCALE");

            ddl.append("  ").append(colName).append(" ");

            if (dataType.contains("VARCHAR2") || dataType.contains("CHAR")) {
                ddl.append(dataType).append("(").append(dataLength).append(")");
            } else if (dataType.equals("NUMBER") && precision != null) {
                if (scale != null && ((Number)scale).intValue() > 0) {
                    ddl.append("NUMBER(").append(precision).append(",").append(scale).append(")");
                } else {
                    ddl.append("NUMBER(").append(precision).append(")");
                }
            } else {
                ddl.append(dataType);
            }
            
            if (i < columns.size() - 1) {
                ddl.append(",");
            }
            ddl.append("\n");
        }

        ddl.append(");\n");
        return ddl.toString();
    }
}