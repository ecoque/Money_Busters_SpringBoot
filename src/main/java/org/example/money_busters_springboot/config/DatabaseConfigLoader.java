package org.example.money_busters_springboot.config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * JavaFX uygulaması için application.yml'den veritabanı bilgilerini okuyan yardımcı sınıf
 */
public class DatabaseConfigLoader {

    private static String url;
    private static String username;
    private static String password;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream inputStream = DatabaseConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.yml")) {
            
            if (inputStream == null) {
                throw new RuntimeException("application.yml bulunamadı!");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(inputStream);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) config.get("spring");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
            
            url = (String) datasource.get("url");
            username = (String) datasource.get("username");
            password = (String) datasource.get("password");
            
        } catch (Exception e) {
            System.err.println("Veritabanı konfigürasyonu yüklenirken hata oluştu: " + e.getMessage());
            throw new RuntimeException("Veritabanı konfigürasyonu yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
}
