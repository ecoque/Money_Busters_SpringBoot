package org.example.money_busters_springboot.ui;

import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import java.util.prefs.Preferences;

public class Launcher {

    // Ayarların kaydedileceği anahtar kelimeler
    private static final String PREF_URL = "db_url";
    private static final String PREF_USER = "db_user";
    private static final String PREF_PASS = "db_pass";
    private static final String PREF_REMEMBER = "remember_me";

    public static void main(String[] args) {
        // Modern Görünüm
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // --- AYARLARI YÜKLE ---
        Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
        boolean isRemembered = prefs.getBoolean(PREF_REMEMBER, false);

        // Varsayılan Değerler (Eğer kayıtlı yoksa bunlar gelir)
        String defaultUrl = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
        String defaultUser = "UPT";
        String defaultPass = "upt123";

        // Eğer "Beni Hatırla" seçiliyse kayıtlıları yükle
        if (isRemembered) {
            defaultUrl = prefs.get(PREF_URL, defaultUrl);
            defaultUser = prefs.get(PREF_USER, defaultUser);
            // Şifreyi çözelim (Base64 decode)
            String encodedPass = prefs.get(PREF_PASS, "");
            if (!encodedPass.isEmpty()) {
                try {
                    defaultPass = new String(Base64.getDecoder().decode(encodedPass));
                } catch (Exception e) { defaultPass = ""; }
            }
        } else {
            // Hatırlama yoksa şifre alanını boş getir (Güvenlik)
            defaultPass = "";
        }

        while (true) {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();
            cs.fill = GridBagConstraints.HORIZONTAL;
            cs.insets = new Insets(5, 5, 5, 5);

            // --- URL ALANI (GİZLİ) ---
            cs.gridx = 0; cs.gridy = 0; panel.add(new JLabel("DB URL:"), cs);
            JPasswordField tfUrl = new JPasswordField(defaultUrl, 20);
            tfUrl.setEchoChar('*'); // Gizli Karakter
            cs.gridx = 1; cs.gridy = 0; panel.add(tfUrl, cs);

            // --- USERNAME ---
            cs.gridx = 0; cs.gridy = 1; panel.add(new JLabel("Username:"), cs);
            JTextField tfUser = new JTextField(defaultUser, 20);
            cs.gridx = 1; cs.gridy = 1; panel.add(tfUser, cs);

            // --- PASSWORD ---
            cs.gridx = 0; cs.gridy = 2; panel.add(new JLabel("Password:"), cs);
            JPasswordField tfPass = new JPasswordField(defaultPass, 20);
            cs.gridx = 1; cs.gridy = 2; panel.add(tfPass, cs);

            // --- BENİ HATIRLA CHECKBOX (YENİ) ---
            cs.gridx = 1; cs.gridy = 3;
            JCheckBox cbRemember = new JCheckBox("Beni Hatırla", isRemembered);
            panel.add(cbRemember, cs);

            // --- DİYALOG ---
            String[] options = {"Bağlan", "İptal"};
            int option = JOptionPane.showOptionDialog(null, panel, "Money Busters - Giriş",
                    JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (option != 0) System.exit(0);

            // Verileri Al
            String url = new String(tfUrl.getPassword()).trim();
            String user = tfUser.getText().trim();
            String pass = new String(tfPass.getPassword()).trim();

            if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Eksik bilgi girdiniz!", "Hata", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // --- KAYIT İŞLEMİ (Save) ---
            if (cbRemember.isSelected()) {
                prefs.put(PREF_URL, url);
                prefs.put(PREF_USER, user);
                // Şifreyi basitçe şifrele (Base64 encode)
                prefs.put(PREF_PASS, Base64.getEncoder().encodeToString(pass.getBytes()));
                prefs.putBoolean(PREF_REMEMBER, true);
            } else {
                // Tiki kaldırdıysa bilgileri unut
                prefs.remove(PREF_URL);
                prefs.remove(PREF_USER);
                prefs.remove(PREF_PASS);
                prefs.putBoolean(PREF_REMEMBER, false);
            }

            // --- SİSTEME YÜKLE ---
            System.setProperty("DB_URL", url);
            System.setProperty("DB_USER", user);
            System.setProperty("DB_PASSWORD", pass);

            // --- BAŞLAT ---
            try {
                MoneyBustersSpringBootApplication.main(args);
                break;
            } catch (Throwable e) {
                e.printStackTrace();
                int retry = JOptionPane.showConfirmDialog(null,
                        "Bağlantı Hatası!\nBilgileri kontrol edip tekrar denemek ister misiniz?",
                        "Hata", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (retry == JOptionPane.NO_OPTION) System.exit(1);
            }
        }
    }
}