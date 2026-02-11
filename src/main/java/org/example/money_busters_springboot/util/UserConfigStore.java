package org.example.money_busters_springboot.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;

/**
 * Kullanıcı giriş bilgilerini (URL, username, password) JSON dosyasında saklar.
 * Windows Registry (java.util.prefs.Preferences) yerine dosya tabanlı çalışır.
 *
 * Dosya konumu: {user.home}/.moneybusters/config.json
 */
public class UserConfigStore {

    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".moneybusters");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private String url = "";
    private String username = "";
    private String encodedPassword = "";
    private boolean rememberMe = false;

    private UserConfigStore() {}

    /**
     * JSON dosyasından config yükler. Dosya yoksa boş config döner.
     */
    public static UserConfigStore load() {
        UserConfigStore config = new UserConfigStore();
        if (!Files.exists(CONFIG_FILE)) {
            return config;
        }
        try {
            String json = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);
            config.url = extractJsonValue(json, "url");
            config.username = extractJsonValue(json, "username");
            config.encodedPassword = extractJsonValue(json, "encodedPassword");
            config.rememberMe = "true".equalsIgnoreCase(extractJsonValue(json, "rememberMe"));
        } catch (Exception e) {
            System.err.println("Config dosyası okunamadı: " + e.getMessage());
        }
        return config;
    }

    /**
     * Config'i JSON dosyasına yazar.
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            String json = "{\n" +
                    "  \"url\": \"" + escapeJson(url) + "\",\n" +
                    "  \"username\": \"" + escapeJson(username) + "\",\n" +
                    "  \"encodedPassword\": \"" + escapeJson(encodedPassword) + "\",\n" +
                    "  \"rememberMe\": " + rememberMe + "\n" +
                    "}";
            Files.writeString(CONFIG_FILE, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.err.println("Config dosyası yazılamadı: " + e.getMessage());
        }
    }

    // --- Getter / Setter ---

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url != null ? url : "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * Şifreyi Base64 encode ederek saklar.
     */
    public void setPassword(String plainPassword) {
        if (plainPassword != null && !plainPassword.isEmpty()) {
            this.encodedPassword = Base64.getEncoder().encodeToString(plainPassword.getBytes(StandardCharsets.UTF_8));
        } else {
            this.encodedPassword = "";
        }
    }

    /**
     * Saklanan şifreyi decode ederek döner.
     */
    public String getDecodedPassword() {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(encodedPassword), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Şifre bilgisini temizler (Hatırla seçilmediğinde).
     */
    public void clearPassword() {
        this.encodedPassword = "";
    }

    /**
     * Config dosyasının yolunu döner.
     */
    public static String getConfigFilePath() {
        return CONFIG_FILE.toString();
    }

    // --- JSON yardımcı ---

    private static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex < 0) return "";

        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex < 0) return "";

        String afterColon = json.substring(colonIndex + 1).trim();

        // Boolean değer
        if (afterColon.startsWith("true") || afterColon.startsWith("false")) {
            return afterColon.startsWith("true") ? "true" : "false";
        }

        // String değer
        if (afterColon.startsWith("\"")) {
            int start = 1;
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < afterColon.length(); i++) {
                char c = afterColon.charAt(i);
                if (c == '\\' && i + 1 < afterColon.length()) {
                    char next = afterColon.charAt(i + 1);
                    if (next == '"') { sb.append('"'); i++; }
                    else if (next == '\\') { sb.append('\\'); i++; }
                    else { sb.append(c); }
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        return "";
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
