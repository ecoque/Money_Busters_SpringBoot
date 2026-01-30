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
    private static boolean isJavaFxLaunched = false; // JavaFX daha önce başladı mı?

    public static void main(String[] args) {
        // 1. Spring Boot Context'i (Backend) Yeniden Başlat
        // Her giriş denemesinde taze bir context oluşturuyoruz.
        if (context != null && context.isActive()) {
            context.close();
        }
        context = SpringApplication.run(MoneyBustersSpringBootApplication.class, args);

        // 2. JavaFX Arayüzünü Başlat (Kritik Kontrol)
        if (!isJavaFxLaunched) {
            // İlk açılışsa normal başlat
            isJavaFxLaunched = true;
            try {
                Application.launch(TriggerCreationApp.class, args);
            } catch (IllegalStateException e) {
                // Nadir de olsa launch hatası olursa elle açmayı dene
                launchManual();
            }
        } else {
            // İkinci açılışsa (Logout sonrası) launch YAPMA! (Hata verir)
            // Halihazırda çalışan JavaFX motoruna "Yeni pencere aç" emri ver.
            launchManual();
        }
    }

    // Elle Pencere Açma Metodu
    private static void launchManual() {
        Platform.runLater(() -> {
            try {
                // Yeni bir TriggerCreationApp örneği oluştur ve başlat
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
        // DİKKAT: Platform.exit() yapmıyoruz! JavaFX açık kalsın.
    }
}