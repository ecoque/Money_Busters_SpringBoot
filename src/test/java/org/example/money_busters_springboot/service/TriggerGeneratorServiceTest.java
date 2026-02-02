package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.repository.TriggerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TriggerGeneratorServiceTest {

    @Mock
    private TriggerRepository triggerRepository;

    private TriggerGeneratorService triggerGeneratorService;

    @BeforeEach
    void setUp() {
        triggerGeneratorService = new TriggerGeneratorService(triggerRepository);
    }

    @Test
    void testGenerateFullTriggerSql_BasariliSenaryo() {
        String schema = "UPT";
        String tableName = "EMPLOYEES";

        List<Map<String, Object>> mockColumns = Arrays.asList(
                createColumn("ID", "NUMBER"),
                createColumn("NAME", "VARCHAR2"),
                createColumn("SALARY", "NUMBER")
        );

        when(triggerRepository.getTableColumns(schema, tableName)).thenReturn(mockColumns);

        String result = triggerGeneratorService.generateFullTriggerSql(schema, tableName);

        assertNotNull(result, "Üretilen SQL null olmamalı");
        assertTrue(result.contains("CREATE OR REPLACE TRIGGER"), "SQL, CREATE TRIGGER içermeli");
        assertTrue(result.contains("TRG_EMPLOYEES"), "Trigger adı doğru olmalı");
        assertTrue(result.contains("EMPLOYEES_HIS"), "History tablo adı doğru olmalı");
        assertTrue(result.contains("SEQ_EMPLOYEES_HIS"), "Sequence adı doğru olmalı");
        assertTrue(result.contains("INSERTING"), "INSERT kontrolü olmalı");
        assertTrue(result.contains("UPDATING"), "UPDATE kontrolü olmalı");
        assertTrue(result.contains("DELETING"), "DELETE kontrolü olmalı");

        verify(triggerRepository, times(1)).getTableColumns(schema, tableName);
    }

    @Test
    void testGenerateHisTableDdl_BasariliSenaryo() {
        String schema = "UPT";
        String tableName = "EMPLOYEES";

        List<Map<String, Object>> mockColumns = Arrays.asList(
                createColumn("ID", "NUMBER", 10, 10, null),
                createColumn("NAME", "VARCHAR2", 100, null, null)
        );

        when(triggerRepository.getTableColumns(schema, tableName)).thenReturn(mockColumns);

        String result = triggerGeneratorService.generateHisTableDdl(schema, tableName);

        assertNotNull(result);
        assertTrue(result.contains("CREATE TABLE"));
        assertTrue(result.contains("EMPLOYEES_HIS"));
        assertTrue(result.contains("HIS_X NUMBER"));
        assertTrue(result.contains("OP_TYPE CHAR(1)"));
    }

    @Test
    void testRollbackMethods_DogruFormat() {
        String schema = "UPT";
        String tableName = "EMPLOYEES";

        String rbTrigger = triggerGeneratorService.rbTrigger(schema, tableName);
        String rbHis = triggerGeneratorService.rbHisTable(schema, tableName);
        String rbSeq = triggerGeneratorService.rbSequence(schema, tableName);
        String rbMain = triggerGeneratorService.rbMainTable(schema, tableName);

        assertNotNull(rbTrigger);
        assertTrue(rbTrigger.contains("DROP TRIGGER UPT.TRG_EMPLOYEES"));

        assertNotNull(rbHis);
        assertTrue(rbHis.contains("DROP TABLE UPT.EMPLOYEES_HIS"));

        assertNotNull(rbSeq);
        assertTrue(rbSeq.contains("DROP SEQUENCE UPT.SEQ_EMPLOYEES_HIS"));

        assertNotNull(rbMain);
        assertTrue(rbMain.contains("DROP TABLE UPT.EMPLOYEES"));
    }

    private Map<String, Object> createColumn(String name, String type) {
        return createColumn(name, type, null, null, null);
    }

    private Map<String, Object> createColumn(String name, String type, Integer length,
                                             Integer precision, Integer scale) {
        Map<String, Object> column = new HashMap<>();
        column.put("COLUMN_NAME", name);
        column.put("DATA_TYPE", type);
        if (length != null) column.put("DATA_LENGTH", length);
        if (precision != null) column.put("DATA_PRECISION", precision);
        if (scale != null) column.put("DATA_SCALE", scale);
        return column;
    }
}