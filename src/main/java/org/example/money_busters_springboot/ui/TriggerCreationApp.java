package org.example.money_busters_springboot.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;
import org.example.money_busters_springboot.service.TriggerService;
import org.example.money_busters_springboot.ui.Launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class TriggerCreationApp extends Application {

    private ComboBox<String> schemaComboBox;
    private ComboBox<String> tableComboBox;
    private RadioButton dbRadioButton;
    private RadioButton scriptOnlyRadioButton;
    private TriggerService triggerService;
    private Stage primaryStage;
    private Image appIcon;

    @Override
    public void start(Stage primaryStage) {
        javafx.application.Platform.setImplicitExit(false);

        this.primaryStage = primaryStage;
        

        try {
            InputStream iconStream = getClass().getResourceAsStream("/icons/trigger_icon.png");
            if (iconStream != null) {
                appIcon = new Image(iconStream);
                primaryStage.getIcons().add(appIcon);
            }
        } catch (Exception e) {
            System.err.println("Icon yüklenemedi: " + e.getMessage());
        }
        
        this.triggerService = MoneyBustersSpringBootApplication.getBean(TriggerService.class);

        VBox mainLayout = new VBox(12);
        mainLayout.setPadding(new Insets(15, 30, 15, 30));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button logoutBtn = new Button("Çıkış Yap");
        logoutBtn.setStyle(
                "-fx-background-color: #e53935;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 4;" +
                        "-fx-font-size: 11px;" +
                        "-fx-cursor: hand;"
        );

        logoutBtn.setOnAction(e -> handleLogout());

        topBar.getChildren().add(logoutBtn);
        HBox radioBox = new HBox(10); 
        radioBox.setAlignment(Pos.CENTER);
        ToggleGroup modeGroup = new ToggleGroup();
        dbRadioButton = new RadioButton("Database'e Kaydet"); 
        dbRadioButton.setToggleGroup(modeGroup); 
        dbRadioButton.setSelected(true);
        scriptOnlyRadioButton = new RadioButton("Sadece Script"); 
        scriptOnlyRadioButton.setToggleGroup(modeGroup);
        radioBox.getChildren().addAll(dbRadioButton, scriptOnlyRadioButton);

        GridPane formGrid = new GridPane(); 
        formGrid.setHgap(12); 
        formGrid.setVgap(12);
        formGrid.setAlignment(Pos.CENTER);

        schemaComboBox = new ComboBox<>(); 
        schemaComboBox.setPrefWidth(280);
        schemaComboBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #BDBDBD;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 6 12;" +
            "-fx-font-size: 13px;"
        );
        
        tableComboBox = new ComboBox<>(); 
        tableComboBox.setPrefWidth(280);
        tableComboBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #BDBDBD;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 6 12;" +
            "-fx-font-size: 13px;"
        );

        Label schemaLabel = new Label("Schema:");
        schemaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label tableLabel = new Label("Table:");
        tableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        formGrid.add(schemaLabel, 0, 0); 
        formGrid.add(schemaComboBox, 1, 0);
        formGrid.add(tableLabel, 0, 1); 
        formGrid.add(tableComboBox, 1, 1);

        Button createBtn = new Button("Trigger Oluştur");
        createBtn.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 20;" +
            "-fx-cursor: hand;"
        );
        createBtn.setOnAction(e -> handleCreateTrigger());

        Button refreshBtn = new Button("Yenile");
        refreshBtn.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 20;" +
            "-fx-cursor: hand;"
        );
        refreshBtn.setOnAction(e -> loadSchemas());

        HBox btnBox = new HBox(15); 
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(createBtn, refreshBtn);

        schemaComboBox.setOnAction(e -> loadTables(schemaComboBox.getValue()));

        mainLayout.getChildren().addAll(topBar, radioBox, formGrid, btnBox);
        loadSchemas();

        primaryStage.setScene(new Scene(mainLayout, 480, 250));
        primaryStage.setTitle("Trigger Automation");
        primaryStage.show();
    }


    private void handleLogout() {
        primaryStage.close();

        MoneyBustersSpringBootApplication.stopApp();


        new Thread(() -> {
            try {
                Launcher.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadSchemas() {
        schemaComboBox.setItems(FXCollections.observableArrayList(triggerService.getAllSchemas()));
    }

    private void loadTables(String schema) {
        if (schema != null) tableComboBox.setItems(FXCollections.observableArrayList(triggerService.getTablesBySchema(schema)));
    }

    private void handleCreateTrigger() {
        String s = schemaComboBox.getValue();
        String t = tableComboBox.getValue();
        if (s == null || t == null) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen şema ve tablo seçiniz.");
            return;
        }

        if (triggerService.checkIfAnyExists(s, t)) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Bu tablo için kayıtlar zaten var.\n\n" +
                            "• Veriler KORUNACAK\n" +
                            "• Scriptler GÜNCELLEME modunda hazırlanacak\n\n" +
                            "Devam etmek istiyor musunuz?", ButtonType.YES, ButtonType.NO);
            a.setTitle("Onay");
            setAlertIcon(a);
            if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }

        try {
            Map<String, String> scripts = triggerService.processTriggerRequest(s, t, dbRadioButton.isSelected());
            showSuccess(s, t, scripts, triggerService.checkIfAnyExists(s, t));
            if (dbRadioButton.isSelected()) loadTables(s);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Hata: " + ex.getMessage());
        }
    }

    private void showSuccess(String s, String t, Map<String, String> scripts, boolean exists) {
        Dialog<Void> d = new Dialog<>(); 
        d.setTitle("Başarılı"); 
        d.setHeaderText("Scriptler Hazır!");

        Stage dialogStage = (Stage) d.getDialogPane().getScene().getWindow();
        if (appIcon != null && dialogStage != null) {
            dialogStage.getIcons().add(appIcon);
        }
        
        VBox v = new VBox(15); 
        v.setPadding(new Insets(20)); 
        v.setAlignment(Pos.CENTER);

        GridPane g = new GridPane(); 
        g.setHgap(10); 
        g.setVgap(10);
        g.add(btn("📋 Main", "#2196F3", t + ".ddl", scripts.get("main")), 0, 0);
        g.add(btn("📜 HIS", "#4CAF50", t + "_HIS.ddl", scripts.get("his")), 1, 0);
        g.add(btn("⚡ Trigger", "#FF9800", "TRG_" + t + ".trg", scripts.get("trigger")), 0, 1);
        g.add(btn("🔢 Seq", "#9C27B0", "SEQ_" + t + ".ddl", scripts.get("seq")), 1, 1);

        Label rbL = new Label("Geri Alma Scriptleri:"); 
        rbL.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        GridPane rbG = new GridPane(); 
        rbG.setHgap(10); 
        rbG.setVgap(10);
        rbG.add(btn("🔄 TRG RB", "#D32F2F", "TRG_" + t + "[RB].ddl", scripts.get("rb_trg")), 0, 0);
        rbG.add(btn("🔄 HIS RB", "#D32F2F", t + "_HIS[RB].ddl", scripts.get("rb_his")), 1, 0);
        rbG.add(btn("🔄 SEQ RB", "#D32F2F", "SEQ_" + t + "[RB].ddl", scripts.get("rb_seq")), 0, 1);
        rbG.add(btn("🔄 MAIN RB", "#D32F2F", t + "[RB].ddl", scripts.get("rb_main")), 1, 1);

        // Tümünü İndir butonu
        Map<String, String> allFiles = new LinkedHashMap<>();
        allFiles.put(t + ".ddl", scripts.get("main"));
        allFiles.put(t + "_HIS.ddl", scripts.get("his"));
        allFiles.put("TRG_" + t + ".trg", scripts.get("trigger"));
        allFiles.put("SEQ_" + t + ".ddl", scripts.get("seq"));
        allFiles.put("TRG_" + t + "[RB].ddl", scripts.get("rb_trg"));
        allFiles.put(t + "_HIS[RB].ddl", scripts.get("rb_his"));
        allFiles.put("SEQ_" + t + "[RB].ddl", scripts.get("rb_seq"));
        allFiles.put(t + "[RB].ddl", scripts.get("rb_main"));

        Button downloadAllBtn = new Button("\uD83D\uDCE5 Tümünü İndir");
        downloadAllBtn.setPrefWidth(260);
        downloadAllBtn.setStyle(
            "-fx-background-color: #37474F;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 13px;"
        );
        downloadAllBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Scriptlerin Kaydedileceği Klasörü Seçin");
            File dir = dc.showDialog(primaryStage);
            if (dir != null) {
                int saved = 0;
                for (Map.Entry<String, String> entry : allFiles.entrySet()) {
                    if (entry.getValue() != null) {
                        try (FileWriter fw = new FileWriter(new File(dir, entry.getKey()))) {
                            fw.write(entry.getValue());
                            saved++;
                        } catch (Exception ignored) {}
                    }
                }
                showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                        saved + " script dosyası kaydedildi:\n" + dir.getAbsolutePath());
            }
        });

        HBox downloadBox = new HBox();
        downloadBox.setAlignment(Pos.CENTER);
        downloadBox.getChildren().add(downloadAllBtn);

        v.getChildren().addAll(new Label(exists ? "Güncelleme Yapıldı" : "Sıfırdan Oluşturuldu"), g, new Separator(), rbL, rbG, new Separator(), downloadBox);
        d.getDialogPane().setContent(v); 
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        d.getDialogPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && appIcon != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.getIcons().add(appIcon);
            }
        });
        
        d.showAndWait();
    }

    private Button btn(String txt, String color, String fname, String content) {
        Button b = new Button(txt); 
        b.setPrefWidth(120);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        b.setOnAction(e -> {
            FileChooser fc = new FileChooser(); 
            fc.setInitialFileName(fname);
            File f = fc.showSaveDialog(primaryStage);
            if (f != null) {
                try (FileWriter fw = new FileWriter(f)) { 
                    fw.write(content); 
                } catch (Exception ignored) {}
            }
        });
        return b;
    }

    private void setAlertIcon(Alert alert) {
        if (appIcon != null) {
            alert.getDialogPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Stage stage = (Stage) newScene.getWindow();
                    stage.getIcons().add(appIcon);
                }
            });
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        setAlertIcon(alert);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}