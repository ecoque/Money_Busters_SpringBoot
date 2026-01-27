package org.example.money_busters_springboot.ui;

import javafx.application.Application;
import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/**
 * JavaFX Launcher sınıfı
 * Spring Boot Context'i başlatır ve JavaFX uygulamasına aktarır
 */
public class Launcher {
    
    public static void main(String[] args) {
        // 1. Spring Boot Context'i başlat
        ApplicationContext springContext = SpringApplication.run(
            MoneyBustersSpringBootApplication.class, args
        );
        
        // 2. Context'i TriggerCreationApp'e aktar
        TriggerCreationApp.setSpringContext(springContext);
        
        // 3. JavaFX uygulamasını başlat
        Application.launch(TriggerCreationApp.class, args);
    }
}
