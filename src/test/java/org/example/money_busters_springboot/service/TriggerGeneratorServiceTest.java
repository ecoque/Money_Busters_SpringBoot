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

/**
 * TriggerGeneratorService için Unit Test
 * Bu test, SQL trigger üretiminin doğru çalışıp çalışmadığını kontrol eder
 */
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
        // Arrange (Hazırlık)
        String schema = "UPT";
        String tableName = "EMPLOYEES";

        // Mock kolon verisi oluştur
        List<Map<String, Object>> mockColumns = Arrays.asList(
                createColumn("ID", "NUMBER"),
                createColumn("NAME", "VARCHAR2"),
                createColumn("SALARY", "NUMBER")
        );

        when(triggerRepository.getTableColumns(schema, tableName)).thenReturn(mockColumns);

        // Act (İşlem)
        String result = triggerGeneratorService.generateFullTriggerSql(schema, tableName);

        // Assert (Doğrulama)
        assertNotNull(result, "Üretilen SQL null olmamalı");
        assertTrue(result.contains("CREATE OR REPLACE TRIGGER"), "SQL, CREATE TRIGGER içermeli");
        assertTrue(result.contains("TRG_EMPLOYEES"), "Trigger adı doğru olmalı");
        assertTrue(result.contains("EMPLOYEES_HIS"), "History tablo adı doğru olmalı");
        assertTrue(result.contains("SEQ_EMPLOYEES_HIS"), "Sequence adı doğru olmalı");
        assertTrue(result.contains("ID, NAME, SALARY"), "Tüm kolonlar SQL'de olmalı");
        assertTrue(result.contains("INSERTING"), "INSERT kontrolü olmalı");
        assertTrue(result.contains("UPDATING"), "UPDATE kontrolü olmalı");
        assertTrue(result.contains("DELETING"), "DELETE kontrolü olmalı");

        // Repository'nin bir kez çağrıldığını doğrula
        verify(triggerRepository, times(1)).getTableColumns(schema, tableName);
    }

    @Test
    void testGenerateFullTriggerSql_TekKolonIle() {
        // Tek kolonlu tablo testi
        String schema = "UPT";
        String tableName = "SIMPLE_TABLE";

        List<Map<String, Object>> mockColumns = Arrays.asList(
                createColumn("ID", "NUMBER")
        );

        when(triggerRepository.getTableColumns(schema, tableName)).thenReturn(mockColumns);

        String result = triggerGeneratorService.generateFullTriggerSql(schema, tableName);

        assertNotNull(result);
        assertTrue(result.contains("ID"));
        assertTrue(result.contains(":OLD.ID"));
        assertTrue(result.contains(":NEW.ID"));
    }

    @Test
    void testGenerateFullTriggerSql_CokluKolonlar() {
        // Çok kolonlu tablo testi
        String schema = "UPT";
        String tableName = "COMPLEX_TABLE";

        List<Map<String, Object>> mockColumns = Arrays.asList(
                createColumn("ID", "NUMBER"),
                createColumn("FIRST_NAME", "VARCHAR2"),
                createColumn("LAST_NAME", "VARCHAR2"),
                createColumn("EMAIL", "VARCHAR2"),
                createColumn("HIRE_DATE", "DATE"),
                createColumn("SALARY", "NUMBER")
        );

        when(triggerRepository.getTableColumns(schema, tableName)).thenReturn(mockColumns);

        String result = triggerGeneratorService.generateFullTriggerSql(schema, tableName);

        assertNotNull(result);
        assertTrue(result.contains("FIRST_NAME"));
        assertTrue(result.contains("LAST_NAME"));
        assertTrue(result.contains("EMAIL"));
        assertTrue(result.contains("HIRE_DATE"));
        assertTrue(result.contains("SALARY"));
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
        assertTrue(result.contains("DML_USER VARCHAR2"));
        assertTrue(result.contains("DML_DATE DATE"));
        assertTrue(result.contains("CREATE SEQUENCE"));
        assertTrue(result.contains("SEQ_EMPLOYEES_HIS"));
    }

    @Test
    void testGenerateRollbackDdl_DogruFormat() {
        String schema = "UPT";
        String tableName = "EMPLOYEES";

        String result = triggerGeneratorService.generateRollbackDdl(schema, tableName);

        assertNotNull(result);
        assertTrue(result.contains("DROP TRIGGER"));
        assertTrue(result.contains("TRG_EMPLOYEES"));
        assertTrue(result.contains("DROP TABLE"));
        assertTrue(result.contains("EMPLOYEES_HIS"));
        assertTrue(result.contains("DROP SEQUENCE"));
        assertTrue(result.contains("SEQ_EMPLOYEES_HIS"));
    }

    // Yardımcı metod - Mock kolon oluşturucu
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
