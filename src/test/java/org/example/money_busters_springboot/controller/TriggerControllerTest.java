package org.example.money_busters_springboot.controller;

import org.example.money_busters_springboot.model.TriggerMetadata;
import org.example.money_busters_springboot.service.TriggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerControllerTest {

    @Mock
    private TriggerService triggerService;

    @InjectMocks
    private TriggerController triggerController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(triggerController, "currentDbUser", "UPT");
    }

    @Test
    void testGetAllTriggers_Success() {
        List<TriggerMetadata> mockTriggers = Arrays.asList(
                new TriggerMetadata("TRG_EMP", "EMPLOYEES", "AFTER EACH ROW", "INSERT", "ENABLED", "BEGIN... END;")
        );
        when(triggerService.getAllTriggers()).thenReturn(mockTriggers);

        ResponseEntity<List<TriggerMetadata>> response = triggerController.getAllTriggers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(triggerService).getAllTriggers();
    }

    @Test
    void testCreateInsertTrigger_Success() {

        String tableName = "EMPLOYEES";


        doNothing().when(triggerService).createInsertTrigger("UPT", tableName.toUpperCase());


        ResponseEntity<String> response = triggerController.createInsertTrigger(tableName, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Trigger oluşturuldu"));
        verify(triggerService).createInsertTrigger("UPT", tableName.toUpperCase());
    }

    @Test
    void testCreateInsertTrigger_WithCustomSchema() {
        String tableName = "EMPLOYEES";
        String customSchema = "OZEL_SCHEMA";

        doNothing().when(triggerService).createInsertTrigger(customSchema, tableName.toUpperCase());

        ResponseEntity<String> response = triggerController.createInsertTrigger(tableName, customSchema);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(customSchema));
        verify(triggerService).createInsertTrigger(customSchema, tableName.toUpperCase());
    }
}