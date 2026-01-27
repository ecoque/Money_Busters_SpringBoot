package org.example.money_busters_springboot;

import javafx.application.Application;
import org.example.money_busters_springboot.ui.TriggerCreationApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MoneyBustersSpringBootApplication {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // 1. Önce Backend'i (Spring Boot) başlat
        context = SpringApplication.run(MoneyBustersSpringBootApplication.class, args);
        // 2. Sonra Arayüzü (JavaFX) başlat
        Application.launch(TriggerCreationApp.class, args);
    }

    // Bu metot sayesinde arayüz, backend servislerini çağırabilecek
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}