package org.example.money_busters_springboot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.money_busters_springboot.ui.TriggerCreationApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MoneyBustersSpringBootApplication {

    private static ConfigurableApplicationContext context;
    private static boolean isJavaFxLaunched = false;

    public static void main(String[] args) {
        if (context != null && context.isActive()) {
            context.close();
        }
        context = SpringApplication.run(MoneyBustersSpringBootApplication.class, args);

        if (!isJavaFxLaunched) {
            isJavaFxLaunched = true;
            try {
                Application.launch(TriggerCreationApp.class, args);
            } catch (IllegalStateException e) {
                launchManual();
            }
        } else {
            launchManual();
        }
    }


    private static void launchManual() {
        Platform.runLater(() -> {
            try {
                new TriggerCreationApp().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static void stopApp() {
        if (context != null) {
            context.close();
            System.out.println("Spring Context kapatıldı.");
        }
    }
}