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

    public String generateInsertTriggerSql(String schema, String tableName) {

        // 1️⃣ Kolonları DB'den çek
        List<Map<String, Object>> columns =
                triggerRepository.getTableColumns(schema, tableName);

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Tabloya ait kolon bulunamadı: " + tableName);
        }

        // 2️⃣ İsimler
        String hisTable = tableName + "_HIS";
        String sequenceName = "SEQ_" + tableName + "_HIS";
        String triggerName = "TRG_" + tableName;

        // 3️⃣ Ana tablo kolonları
        List<String> tableColumns = columns.stream()
                .map(c -> c.get("COLUMN_NAME").toString())
                .collect(Collectors.toList());

        // 4️⃣ HIS kolon listesi
        String hisColumns = String.join(", ",
                "HIS_X",
                "OP_TYPE",
                "DML_USER",
                "DML_DATE",
                String.join(", ", tableColumns)
        );

        // 5️⃣ VALUES listesi
        String values = String.join(", ",
                schema + "." + sequenceName + ".NEXTVAL",
                "'I'",
                "SYS_CONTEXT('USERENV','SESSION_USER')",
                "SYSDATE",
                tableColumns.stream()
                        .map(c -> ":NEW." + c)
                        .collect(Collectors.joining(", "))
        );

        // 6️⃣ TRIGGER SQL
        return String.format("""
            CREATE OR REPLACE TRIGGER %s.%s
            AFTER INSERT
            ON %s.%s
            FOR EACH ROW
            BEGIN
                INSERT INTO %s.%s (
                    %s
                ) VALUES (
                    %s
                );
            END;
            """,
                schema,
                triggerName,
                schema,
                tableName,
                schema,
                hisTable,
                hisColumns,
                values
        );
    }
}
