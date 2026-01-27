package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TriggerService için Unit Test
 * Bu test, trigger yönetim işlemlerinin doğru çalışıp çalışmadığını kontrol eder
 */
@ExtendWith(MockitoExtension.class)
class TriggerServiceTest {

    @Mock
    private TriggerRepository triggerRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private TriggerGeneratorService triggerGeneratorService;

    private TriggerService triggerService;

    @BeforeEach
    void setUp() {
        triggerService = new TriggerService(triggerRepository, jdbcTemplate, triggerGeneratorService);
    }

    @Test
    void testGetAllTriggers_BasariliSenaryo() {
        // Arrange - TriggerMetadata constructor: triggerName, tableName, triggerType, triggeringEvent, status, triggerBody
        List<TriggerMetadata> mockTriggers = Arrays.asList(
                new TriggerMetadata("TRG_EMP", "EMPLOYEES", "AFTER EACH ROW", "INSERT OR UPDATE OR DELETE", "ENABLED", "BEGIN... END;"),
                new TriggerMetadata("TRG_DEPT", "DEPARTMENTS", "AFTER EACH ROW", "INSERT OR UPDATE", "ENABLED", "BEGIN... END;")
        );

        when(triggerRepository.findAllTriggers()).thenReturn(mockTriggers);

        // Act
        List<TriggerMetadata> result = triggerService.getAllTriggers();

        // Assert
        assertNotNull(result, "Sonuç null olmamalı");
        assertEquals(2, result.size(), "2 trigger dönmeli");
        assertEquals("TRG_EMP", result.get(0).getTriggerName());
        assertEquals("TRG_DEPT", result.get(1).getTriggerName());

        verify(triggerRepository, times(1)).findAllTriggers();
    }

    @Test
    void testGetTriggersByTable_BasariliSenaryo() {
        // Arrange
        String tableName = "EMPLOYEES";
        List<TriggerMetadata> mockTriggers = Arrays.asList(
                new TriggerMetadata("TRG_EMP_INSERT", tableName, "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;"),
                new TriggerMetadata("TRG_EMP_UPDATE", tableName, "AFTER EACH ROW", "UPDATE", "DISABLED", "BEGIN... END;")
        );

        when(triggerRepository.findTriggersByTableName(tableName)).thenReturn(mockTriggers);

        // Act
        List<TriggerMetadata> result = triggerService.getTriggersByTable(tableName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTableName().equals(tableName)));

        verify(triggerRepository, times(1)).findTriggersByTableName(tableName);
    }

    @Test
    void testGetTriggerByName_TriggerBulundu() {
        // Arrange
        String triggerName = "TRG_EMP";
        TriggerMetadata mockTrigger = new TriggerMetadata(
                triggerName, "EMPLOYEES", "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;"
        );

        when(triggerRepository.findTriggerByName(triggerName)).thenReturn(mockTrigger);

        // Act
        TriggerMetadata result = triggerService.getTriggerByName(triggerName);

        // Assert
        assertNotNull(result);
        assertEquals(triggerName, result.getTriggerName());
        assertEquals("ENABLED", result.getStatus());

        verify(triggerRepository, times(1)).findTriggerByName(triggerName);
    }

    @Test
    void testGetTriggerByName_TriggerBulunamadi() {
        // Arrange
        String triggerName = "NONEXISTENT_TRIGGER";
        when(triggerRepository.findTriggerByName(triggerName)).thenReturn(null);

        // Act
        TriggerMetadata result = triggerService.getTriggerByName(triggerName);

        // Assert
        assertNull(result, "Bulunamayan trigger null dönmeli");

        verify(triggerRepository, times(1)).findTriggerByName(triggerName);
    }

    @Test
    void testEnableTrigger_BasariliSenaryo() {
        // Arrange
        String triggerName = "TRG_EMP";
        doNothing().when(triggerRepository).setTriggerStatus(triggerName, true);

        // Act & Assert
        assertDoesNotThrow(() -> triggerService.enableTrigger(triggerName));

        verify(triggerRepository, times(1)).setTriggerStatus(triggerName, true);
    }

    @Test
    void testDisableTrigger_BasariliSenaryo() {
        // Arrange
        String triggerName = "TRG_EMP";
        doNothing().when(triggerRepository).setTriggerStatus(triggerName, false);

        // Act & Assert
        assertDoesNotThrow(() -> triggerService.disableTrigger(triggerName));

        verify(triggerRepository, times(1)).setTriggerStatus(triggerName, false);
    }

    @Test
    void testGenerateAllScripts_BasariliSenaryo() {
        // Arrange
        String tableName = "EMPLOYEES";

        String mockTriggerSql = "CREATE OR REPLACE TRIGGER...";
        String mockHisTableDdl = "CREATE TABLE...";
        String mockRollbackDdl = "DROP TRIGGER...";

        when(triggerGeneratorService.generateFullTriggerSql("UPT", tableName)).thenReturn(mockTriggerSql);
        when(triggerGeneratorService.generateHisTableDdl("UPT", tableName)).thenReturn(mockHisTableDdl);
        when(triggerGeneratorService.generateRollbackDdl("UPT", tableName)).thenReturn(mockRollbackDdl);

        // Act
        Map<String, String> result = triggerService.generateAllScripts(tableName);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockHisTableDdl, result.get("main.ddl"));
        assertEquals(mockRollbackDdl, result.get("rollback[RB].ddl"));
        assertEquals(mockTriggerSql, result.get("trigger.trg"));

        verify(triggerGeneratorService, times(1)).generateFullTriggerSql("UPT", tableName);
        verify(triggerGeneratorService, times(1)).generateHisTableDdl("UPT", tableName);
        verify(triggerGeneratorService, times(1)).generateRollbackDdl("UPT", tableName);
    }

    @Test
    void testCreateInsertTrigger_BasariliSenaryo() {
        // Arrange
        String schema = "UPT";
        String tableName = "EMPLOYEES";
        String mockSql = "CREATE OR REPLACE TRIGGER...";

        when(triggerGeneratorService.generateFullTriggerSql(schema, tableName)).thenReturn(mockSql);
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act & Assert
        assertDoesNotThrow(() -> triggerService.createInsertTrigger(schema, tableName));

        verify(triggerGeneratorService, times(1)).generateFullTriggerSql(schema, tableName);
        verify(jdbcTemplate, times(1)).execute(mockSql);
    }
}
