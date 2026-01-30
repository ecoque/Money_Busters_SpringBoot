package org.example.money_busters_springboot.ui;

import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Base64;
import java.util.prefs.Preferences;

public class Launcher {

    // Sadece son girilen kullanıcı adını ve URL'i genel tutuyoruz.
    private static final String PREF_LAST_USER = "last_user";
    private static final String PREF_LAST_URL = "last_url";

    // Şifreler artık dinamik olarak saklanacak: "pass_KULLANICIADI" şeklinde.

    public static void main(String[] args) {
        // Modern Görünüm


        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        Preferences prefs = Preferences.userNodeForPackage(Launcher.class);

        // 1. Son kullanılan URL ve Kullanıcı Adını getir
        String defaultUrl = prefs.get(PREF_LAST_URL, "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String lastUser = prefs.get(PREF_LAST_USER, "");

        // O kullanıcının şifresini bulmaya çalış
        String loadedPass = "";
        boolean isRemembered = false;

        if (!lastUser.isEmpty()) {
            String savedPass = prefs.get("pass_" + lastUser, "");
            if (!savedPass.isEmpty()) {
                try {
                    loadedPass = new String(Base64.getDecoder().decode(savedPass));
                    isRemembered = true; // Şifre varsa "Beni Hatırla" tikli gelsin
                } catch (Exception e) { loadedPass = ""; }
            }
        }

        while (true) {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();
            cs.fill = GridBagConstraints.HORIZONTAL;
            cs.insets = new Insets(5, 5, 5, 5);

            // --- URL ---
            cs.gridx = 0; cs.gridy = 0; panel.add(new JLabel("DB URL:"), cs);
            JPasswordField tfUrl = new JPasswordField(defaultUrl, 20);
            tfUrl.setEchoChar('*');
            cs.gridx = 1; cs.gridy = 0; panel.add(tfUrl, cs);

            // --- USERNAME ---
            cs.gridx = 0; cs.gridy = 1; panel.add(new JLabel("Username:"), cs);
            JTextField tfUser = new JTextField(lastUser, 20);
            cs.gridx = 1; cs.gridy = 1; panel.add(tfUser, cs);

            // --- PASSWORD ---
            cs.gridx = 0; cs.gridy = 2; panel.add(new JLabel("Password:"), cs);
            JPasswordField tfPass = new JPasswordField(loadedPass, 20);
            cs.gridx = 1; cs.gridy = 2; panel.add(tfPass, cs);

            // --- BENİ HATIRLA ---
            cs.gridx = 1; cs.gridy = 3;
            JCheckBox cbRemember = new JCheckBox("Şifreyi Hatırla", isRemembered);
            panel.add(cbRemember, cs);

            // --- AKILLI DOLDURMA (User değişince şifreyi getir) ---
            tfUser.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    String currentUser = tfUser.getText().trim();
                    if (!currentUser.isEmpty()) {
                        // Bu kullanıcı için kayıtlı şifre var mı bak
                        String savedPass = prefs.get("pass_" + currentUser, "");
                        if (!savedPass.isEmpty()) {
                            try {
                                String decoded = new String(Base64.getDecoder().decode(savedPass));
                                tfPass.setText(decoded);
                                cbRemember.setSelected(true);
                            } catch (Exception ex) { }
                        } else {
                            // Yeni kullanıcı girdiyse şifre alanını temizle
                            tfPass.setText("");
                            cbRemember.setSelected(false);
                        }
                    }
                }
            });

            // --- DİYALOG ---
            String[] options = {"Bağlan", "İptal"};
            int option = JOptionPane.showOptionDialog(null, panel, "Money Busters - Güvenli Giriş",
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

            // --- KAYIT MANTIĞI (DÜZELTİLDİ) ---
            // Her zaman son URL ve Kullanıcıyı kaydet (Kolaylık olsun)
            prefs.put(PREF_LAST_URL, url);
            prefs.put(PREF_LAST_USER, user);

            if (cbRemember.isSelected()) {
                // Şifreyi BU KULLANICI ADINA ÖZEL anahtarla kaydet
                // Örn: pass_UPT, pass_KEREM, pass_AHMET
                String encodedPass = Base64.getEncoder().encodeToString(pass.getBytes());
                prefs.put("pass_" + user, encodedPass);
            } else {
                // Tiki kaldırdıysa SADECE BU KULLANICININ şifresini unut
                prefs.remove("pass_" + user);
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