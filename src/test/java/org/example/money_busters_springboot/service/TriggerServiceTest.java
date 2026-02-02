package org.example.money_busters_springboot.service;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.repository.TriggerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        triggerService = new TriggerService(triggerRepository, triggerGeneratorService, jdbcTemplate);

        ReflectionTestUtils.setField(triggerService, "currentDbUser", "UPT");
    }

    @Test
    void testGetAllTriggers_BasariliSenaryo() {
        List<TriggerMetadata> mockTriggers = Arrays.asList(
                new TriggerMetadata("TRG_EMP", "EMPLOYEES", "AFTER EACH ROW", "INSERT OR UPDATE OR DELETE", "ENABLED", "BEGIN... END;"),
                new TriggerMetadata("TRG_DEPT", "DEPARTMENTS", "AFTER EACH ROW", "INSERT OR UPDATE", "ENABLED", "BEGIN... END;")
        );

        when(triggerRepository.findAllTriggers()).thenReturn(mockTriggers);

        List<TriggerMetadata> result = triggerService.getAllTriggers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(triggerRepository, times(1)).findAllTriggers();
    }

    @Test
    void testGetTriggersByTable_BasariliSenaryo() {
        String tableName = "EMPLOYEES";
        List<TriggerMetadata> mockTriggers = Arrays.asList(
                new TriggerMetadata("TRG_EMP_INSERT", tableName, "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;")
        );
        when(triggerRepository.findTriggersByTableName(tableName)).thenReturn(mockTriggers);

        List<TriggerMetadata> result = triggerService.getTriggersByTable(tableName);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(triggerRepository, times(1)).findTriggersByTableName(tableName);
    }

    @Test
    void testGenerateAllScripts_BasariliSenaryo() {

        String tableName = "EMPLOYEES";

        String mockTriggerSql = "CREATE TRIGGER...";
        String mockHisTableDdl = "CREATE TABLE...";


        when(triggerGeneratorService.generateFullTriggerSql("UPT", tableName)).thenReturn(mockTriggerSql);
        when(triggerGeneratorService.generateHisTableDdl("UPT", tableName)).thenReturn(mockHisTableDdl);


        when(triggerRepository.historyTableExists("UPT", tableName + "_HIS")).thenReturn(false);


        Map<String, String> result = triggerService.generateAllScripts(tableName);


        assertNotNull(result);
        verify(triggerGeneratorService, times(1)).generateFullTriggerSql("UPT", tableName);
    }

    @Test
    void testCreateInsertTrigger_TekParametreli() {

        String tableName = "EMPLOYEES";
        String mockSql = "CREATE TRIGGER...";


        when(triggerGeneratorService.generateFullTriggerSql("UPT", tableName)).thenReturn(mockSql);
        when(triggerRepository.historyTableExists("UPT", tableName + "_HIS")).thenReturn(false);


        triggerService.createInsertTrigger(tableName);


        verify(triggerGeneratorService).generateFullTriggerSql("UPT", tableName);
    }

    @Test
    void testCreateInsertTrigger_CiftParametreli() {
        String ozelSchema = "OZEL_SCHEMA";
        String tableName = "EMPLOYEES";
        String mockSql = "CREATE TRIGGER...";

        when(triggerGeneratorService.generateFullTriggerSql(ozelSchema, tableName)).thenReturn(mockSql);
        when(triggerRepository.historyTableExists(ozelSchema, tableName + "_HIS")).thenReturn(false);

        triggerService.createInsertTrigger(ozelSchema, tableName);

        verify(triggerGeneratorService).generateFullTriggerSql(ozelSchema, tableName);
    }
}