package org.example.money_busters_springboot.ui;

import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;

/**
 * JavaFX Launcher sınıfı
 * Projenin başlangıç noktası.
 * Spring Boot context'ini ve JavaFX'i doğru sırayla başlatır
 */
public class Launcher {

    public static void main(String[] args) {
        // ESKİ HALİ: TriggerCreationApp.main(args); <- BU ARTIK HATALI (Çünkü Spring yok)

        // YENİ HALİ: Ana Application sınıfını çağırıyoruz.
        // Bu sayede önce Spring Boot (Backend), sonra JavaFX (UI) açılıyor.
        MoneyBustersSpringBootApplication.main(args);
    }
}