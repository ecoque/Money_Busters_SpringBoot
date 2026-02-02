package org.example.money_busters_springboot.ui;

import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Base64;
import java.util.prefs.Preferences;

public class Launcher {

    private static final String PREF_LAST_USER = "last_user";
    private static final String PREF_LAST_URL = "last_url";


    public static void main(String[] args) {


        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        try {
            URL iconUrl = Launcher.class.getResource("/icons/trigger_icon.png");
            if (iconUrl != null) {
                Image appIcon = Toolkit.getDefaultToolkit().getImage(iconUrl);
                if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    Taskbar.getTaskbar().setIconImage(appIcon);
                }
            }
        } catch (Exception ignored) {}

        Preferences prefs = Preferences.userNodeForPackage(Launcher.class);

        String defaultUrl = prefs.get(PREF_LAST_URL, "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String lastUser = prefs.get(PREF_LAST_USER, "");

        String loadedPass = "";
        boolean isRemembered = false;

        if (!lastUser.isEmpty()) {
            String savedPass = prefs.get("pass_" + lastUser, "");
            if (!savedPass.isEmpty()) {
                try {
                    loadedPass = new String(Base64.getDecoder().decode(savedPass));
                    isRemembered = true;
                } catch (Exception e) { loadedPass = ""; }
            }
        }

        while (true) {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();
            cs.fill = GridBagConstraints.HORIZONTAL;
            cs.insets = new Insets(5, 5, 5, 5);

            cs.gridx = 0; cs.gridy = 0; panel.add(new JLabel("DB URL:"), cs);
            JPasswordField tfUrl = new JPasswordField(defaultUrl, 20);
            tfUrl.setEchoChar('*');
            cs.gridx = 1; cs.gridy = 0; panel.add(tfUrl, cs);

            cs.gridx = 0; cs.gridy = 1; panel.add(new JLabel("Username:"), cs);
            JTextField tfUser = new JTextField(lastUser, 20);
            cs.gridx = 1; cs.gridy = 1; panel.add(tfUser, cs);

            cs.gridx = 0; cs.gridy = 2; panel.add(new JLabel("Password:"), cs);
            JPasswordField tfPass = new JPasswordField(loadedPass, 20);
            loadedPass = "";
            cs.gridx = 1; cs.gridy = 2; panel.add(tfPass, cs);

            cs.gridx = 1; cs.gridy = 3;
            JCheckBox cbRemember = new JCheckBox("Şifreyi Hatırla", isRemembered);
            panel.add(cbRemember, cs);

            JFrame ghostFrame = new JFrame("Money Busters Giriş");
            URL iconUrl = Launcher.class.getResource("/icons/trigger_icon.png");
            if (iconUrl != null) {
                ghostFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
            }
            ghostFrame.setUndecorated(true);
            ghostFrame.setVisible(true);
            ghostFrame.setLocationRelativeTo(null);

            String[] options = {"Bağlan", "İptal"};
            int option = JOptionPane.showOptionDialog(ghostFrame, panel, "Money Busters - Güvenli Giriş",
                    JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            ghostFrame.dispose();

            if (option != 0) System.exit(0);

            String url = new String(tfUrl.getPassword()).trim();
            String user = tfUser.getText().trim();
            String pass = new String(tfPass.getPassword()).trim();

            if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Eksik bilgi girdiniz!", "Hata", JOptionPane.WARNING_MESSAGE);
                continue;
            }


            prefs.put(PREF_LAST_URL, url);
            prefs.put(PREF_LAST_USER, user);

            if (cbRemember.isSelected()) {
                String encodedPass = Base64.getEncoder().encodeToString(pass.getBytes());
                prefs.put("pass_" + user, encodedPass);
            } else {
                prefs.remove("pass_" + user);
            }

            System.setProperty("DB_URL", url);
            System.setProperty("DB_USER", user);
            System.setProperty("DB_PASSWORD", pass);


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