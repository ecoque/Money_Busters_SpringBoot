package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.service.TriggerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TriggerController için Unit Test
 * Bu test, REST API endpoint'lerinin iş mantığının doğru çalışıp çalışmadığını kontrol eder
 */
@ExtendWith(MockitoExtension.class)
class TriggerControllerTest {

    @Mock
    private TriggerService triggerService;

    @InjectMocks
    private TriggerController triggerController;

    @Test
    void testGetAllTriggers_Success() {
        // Arrange
        List<TriggerMetadata> mockTriggers = Arrays.asList(
                new TriggerMetadata("TRG_EMP", "EMPLOYEES", "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;"),
                new TriggerMetadata("TRG_DEPT", "DEPARTMENTS", "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;")
        );

        when(triggerService.getAllTriggers()).thenReturn(mockTriggers);

        // Act
        ResponseEntity<List<TriggerMetadata>> response = triggerController.getAllTriggers();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(triggerService, times(1)).getAllTriggers();
    }

    @Test
    void testGetTriggersByTable_Success() {
        // Arrange
        String tableName = "EMPLOYEES";
        List<TriggerMetadata> mockTriggers = List.of(
                new TriggerMetadata("TRG_EMP_1", tableName, "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;")
        );

        when(triggerService.getTriggersByTable(tableName)).thenReturn(mockTriggers);

        // Act
        ResponseEntity<List<TriggerMetadata>> response = triggerController.getTriggersByTable(tableName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(triggerService, times(1)).getTriggersByTable(tableName);
    }

    @Test
    void testGetTriggerByName_Found() {
        // Arrange
        String triggerName = "TRG_EMP";
        TriggerMetadata mockTrigger = new TriggerMetadata(
                triggerName, "EMPLOYEES", "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;"
        );

        when(triggerService.getTriggerByName(triggerName)).thenReturn(mockTrigger);

        // Act
        ResponseEntity<TriggerMetadata> response = triggerController.getTriggerByName(triggerName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(triggerName, response.getBody().getTriggerName());

        verify(triggerService, times(1)).getTriggerByName(triggerName);
    }

    @Test
    void testGetTriggerByName_NotFound() {
        // Arrange
        String triggerName = "NONEXISTENT";
        when(triggerService.getTriggerByName(triggerName)).thenReturn(null);

        // Act
        ResponseEntity<TriggerMetadata> response = triggerController.getTriggerByName(triggerName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(triggerService, times(1)).getTriggerByName(triggerName);
    }

    @Test
    void testEnableTrigger_Success() {
        // Arrange
        String triggerName = "TRG_EMP";
        doNothing().when(triggerService).enableTrigger(triggerName);

        // Act
        ResponseEntity<Map<String, String>> response = triggerController.enableTrigger(triggerName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));

        verify(triggerService, times(1)).enableTrigger(triggerName);
    }

    @Test
    void testEnableTrigger_Error() {
        // Arrange
        String triggerName = "TRG_EMP";
        doThrow(new RuntimeException("Database error")).when(triggerService).enableTrigger(triggerName);

        // Act
        ResponseEntity<Map<String, String>> response = triggerController.enableTrigger(triggerName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));

        verify(triggerService, times(1)).enableTrigger(triggerName);
    }

    @Test
    void testDisableTrigger_Success() {
        // Arrange
        String triggerName = "TRG_EMP";
        doNothing().when(triggerService).disableTrigger(triggerName);

        // Act
        ResponseEntity<Map<String, String>> response = triggerController.disableTrigger(triggerName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));

        verify(triggerService, times(1)).disableTrigger(triggerName);
    }

    @Test
    void testCreateInsertTrigger_Success() {
        // Arrange
        String tableName = "EMPLOYEES";
        doNothing().when(triggerService).createInsertTrigger("UPT", tableName.toUpperCase());

        // Act
        ResponseEntity<String> response = triggerController.createInsertTrigger(tableName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Trigger oluşturuldu"));

        verify(triggerService, times(1)).createInsertTrigger("UPT", tableName.toUpperCase());
    }

    @Test
    void testCreateInsertTrigger_Error() {
        // Arrange
        String tableName = "EMPLOYEES";
        doThrow(new RuntimeException("Table not found"))
                .when(triggerService).createInsertTrigger("UPT", tableName.toUpperCase());

        // Act
        ResponseEntity<String> response = triggerController.createInsertTrigger(tableName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Hata:"));
    }
}
