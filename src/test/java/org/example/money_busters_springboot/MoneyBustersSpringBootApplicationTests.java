package org.example.money_busters_springboot;

import org.example.money_busters_springboot.service.TriggerGeneratorService;
import org.example.money_busters_springboot.service.TriggerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Boot Application Context Test
 * Bu test, uygulamanın başarıyla başlayıp başlamadığını ve 
 * tüm bean'lerin doğru yüklenip yüklenmediğini kontrol eder
 */
@SpringBootTest
class MoneyBustersSpringBootApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Context'in başarıyla yüklendiğini doğrula
        assertNotNull(applicationContext, "Application context yüklenmeliydi");
    }

    @Test
    void testTriggerServiceBeanExists() {
        // TriggerService bean'inin var olduğunu kontrol et
        TriggerService triggerService = applicationContext.getBean(TriggerService.class);
        assertNotNull(triggerService, "TriggerService bean bulunmalıydı");
    }

    @Test
    void testTriggerGeneratorServiceBeanExists() {
        // TriggerGeneratorService bean'inin var olduğunu kontrol et
        TriggerGeneratorService generatorService = applicationContext.getBean(TriggerGeneratorService.class);
        assertNotNull(generatorService, "TriggerGeneratorService bean bulunmalıydı");
    }

    @Test
    void testAllRequiredBeansAreLoaded() {
        // Gerekli tüm bean'lerin yüklendiğini doğrula
        assertAll("Tüm servis bean'leri yüklenmeli",
                () -> assertNotNull(applicationContext.getBean(TriggerService.class)),
                () -> assertNotNull(applicationContext.getBean(TriggerGeneratorService.class))
        );
    }
}
