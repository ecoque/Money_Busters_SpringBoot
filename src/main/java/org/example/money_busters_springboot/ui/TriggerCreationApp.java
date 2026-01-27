package org.example.money_busters_springboot.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.money_busters_springboot.service.TriggerService;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Trigger Oluşturma JavaFX Uygulaması
 * Tüm iş mantığı TriggerService üzerinden yapılır
 */
public class TriggerCreationApp extends Application {

    // Spring Context (Launcher'dan set edilecek)
    private static ApplicationContext springContext;

    // Backend Servisi
    private TriggerService triggerService;

    // UI bileşenleri
    private ComboBox<String> schemaComboBox;
    private ComboBox<String> tableComboBox;
    private Button createTriggerButton;
    private Button refreshButton;
    private Stage primaryStage;
    
    // Checkbox'lar
    private RadioButton dbRadioButton;
    private RadioButton scriptOnlyRadioButton;

    /**
     * Spring Context'i set eder (Launcher'dan çağrılır)
     */
    public static void setSpringContext(ApplicationContext context) {
        springContext = context;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Spring Context'ten TriggerService al
        if (springContext != null) {
            triggerService = springContext.getBean(TriggerService.class);
        } else {
            showAlert(Alert.AlertType.ERROR, "Hata", "Spring Context yüklenemedi!");
            Platform.exit();
            return;
        }

        primaryStage.setTitle("Trigger Oluşturma");

        // Ana layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20, 30, 20, 30));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Üst kısım: Başlık + RadioButton'lar
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        
        // Başlık
        Label titleLabel = new Label("Trigger Oluşturma");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #333333;");
        
        // Sağ üst köşe için RadioButton'lar
        HBox radioBox = new HBox(10);
        radioBox.setAlignment(Pos.CENTER_RIGHT);
        radioBox.setPadding(new Insets(0, 0, 0, 20));
        
        ToggleGroup modeGroup = new ToggleGroup();
        
        dbRadioButton = new RadioButton("Database'e Kaydet");
        dbRadioButton.setToggleGroup(modeGroup);
        dbRadioButton.setSelected(true);
        dbRadioButton.setStyle("-fx-font-size: 11px;");
        
        scriptOnlyRadioButton = new RadioButton("Sadece Script");
        scriptOnlyRadioButton.setToggleGroup(modeGroup);
        scriptOnlyRadioButton.setStyle("-fx-font-size: 11px;");
        
        radioBox.getChildren().addAll(dbRadioButton, scriptOnlyRadioButton);
        
        // Spacer ekle (başlık solda, radiobutton'lar sağda)
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        topBox.getChildren().addAll(titleLabel, radioBox);

        // Form alanları için Grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        // Schema Label ve ComboBox
        Label schemaLabel = new Label("Schema:");
        schemaLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        schemaComboBox = new ComboBox<>();
        schemaComboBox.setPrefWidth(250);
        schemaComboBox.setPromptText("Şema seçiniz...");
        schemaComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 3;");
        
        // Schema değiştiğinde tabloları yükle
        schemaComboBox.setOnAction(e -> {
            if (schemaComboBox.getValue() != null) {
                loadTables(schemaComboBox.getValue());
            }
        });

        // Table Label ve ComboBox
        Label tableLabel = new Label("Table:");
        tableLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        tableComboBox = new ComboBox<>();
        tableComboBox.setPrefWidth(250);
        tableComboBox.setPromptText("Tablo seçiniz...");
        tableComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 3;");

        // Grid'e elemanları ekle
        formGrid.add(schemaLabel, 0, 0);
        formGrid.add(schemaComboBox, 1, 0);
        formGrid.add(tableLabel, 0, 1);
        formGrid.add(tableComboBox, 1, 1);

        // Butonlar
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        createTriggerButton = new Button("Trigger Oluştur");
        createTriggerButton.setPrefWidth(120);
        createTriggerButton.setPrefHeight(35);
        createTriggerButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        refreshButton = new Button("Yenile");
        refreshButton.setPrefWidth(80);
        refreshButton.setPrefHeight(35);
        refreshButton.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        buttonBox.getChildren().addAll(createTriggerButton, refreshButton);

        // Buton hover efektleri
        addHoverEffect(createTriggerButton, "#45a049", "#4CAF50");
        addHoverEffect(refreshButton, "#1976D2", "#2196F3");

        // Event handlers
        refreshButton.setOnAction(e -> {
            loadSchemas();
            if (schemaComboBox.getValue() != null) {
                loadTables(schemaComboBox.getValue());
            }
        });
        
        createTriggerButton.setOnAction(e -> handleCreateTrigger());

        // Ana layout'a ekle
        mainLayout.getChildren().addAll(topBox, formGrid, buttonBox);

        // Şemaları yükle
        loadSchemas();

        // Scene oluştur
        Scene scene = new Scene(mainLayout, 450, 220);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    /**
     * Trigger oluştur butonuna basıldığında çalışır
     */
    private void handleCreateTrigger() {
        String selectedTable = tableComboBox.getValue();
        String schema = schemaComboBox.getValue();
        
        if (schema == null || schema.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir şema seçiniz.");
            return;
        }
        
        if (selectedTable == null || selectedTable.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir tablo seçiniz.");
            return;
        }
        
        // Otomatik isimler
        String triggerName = "TRG_" + selectedTable.toUpperCase();
        String hisTableName = selectedTable.toUpperCase() + "_HIS";
        String seqName = "SEQ_" + hisTableName;
        
        try {
            if (dbRadioButton.isSelected()) {
                // Database'e kaydet modu - TriggerService kullan
                if (triggerService.checkHistoryTableExists(schema, selectedTable)) {
                    // Uyarı göster
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Uyarı");
                    confirmAlert.setHeaderText("Bu trigger daha önce oluşturulmuş!");
                    confirmAlert.setContentText(
                        "'" + hisTableName + "' tablosu zaten mevcut.\n\n" +
                        "Mevcut HIS tablosuna dokunulmayacak, veriler korunacak.\n" +
                        "Sadece trigger yeniden oluşturulacak.\n\n" +
                        "Devam etmek istiyor musunuz?"
                    );
                    
                    ButtonType yesButton = new ButtonType("Evet, Devam Et");
                    ButtonType noButton = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
                    confirmAlert.getButtonTypes().setAll(yesButton, noButton);
                    
                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == yesButton) {
                            try {
                                // Sadece trigger'ı yeniden oluştur - TriggerService kullan
                                triggerService.recreateTriggerOnly(schema, selectedTable);
                                showSuccessDialogWithDownloadButtons(schema, selectedTable, triggerName, hisTableName, seqName);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Hata", "Trigger oluşturulurken hata: " + ex.getMessage());
                            }
                        }
                    });
                    return;
                }
                
                // HIS tablosu yoksa normal akış - TriggerService kullan
                triggerService.createTriggerWithHistoryTable(schema, selectedTable);
                showSuccessDialogWithDownloadButtons(schema, selectedTable, triggerName, hisTableName, seqName);
                loadTables(schema);
                
            } else {
                // Sadece script modu - Database'e hiç dokunma
                showSuccessDialogWithDownloadButtons(schema, selectedTable, triggerName, hisTableName, seqName);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", 
                "İşlem sırasında hata oluştu:\n" + ex.getMessage());
        }
    }
    
    /**
     * Veritabanındaki şemaları yükler - TriggerService kullanır
     */
    private void loadSchemas() {
        try {
            List<String> schemas = triggerService.getAllSchemas();
            
            ObservableList<String> schemaList = FXCollections.observableArrayList(schemas);
            schemaComboBox.setItems(schemaList);
            
            // UPT varsa onu seç, yoksa ilkini seç
            if (schemas.contains("UPT")) {
                schemaComboBox.setValue("UPT");
                loadTables("UPT");
            } else if (!schemas.isEmpty()) {
                schemaComboBox.setValue(schemas.get(0));
                loadTables(schemas.get(0));
            }
            
            System.out.println("Yüklenen şema sayısı: " + schemas.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Şemalar yüklenirken hata oluştu:\n" + e.getMessage());
        }
    }

    /**
     * Belirtilen şemadaki tabloları yükler - TriggerService kullanır
     */
    private void loadTables(String schema) {
        try {
            List<String> tables = triggerService.getTablesBySchema(schema);
            
            ObservableList<String> tableList = FXCollections.observableArrayList(tables);
            tableComboBox.setItems(tableList);
            
            if (!tables.isEmpty()) {
                tableComboBox.setValue(tables.get(0));
            } else {
                tableComboBox.setValue(null);
                tableComboBox.setPromptText("Tablo bulunamadı...");
            }
            
            System.out.println("Yüklenen tablo sayısı (" + schema + "): " + tables.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Tablolar yüklenirken hata oluştu:\n" + e.getMessage());
        }
    }

    /**
     * 4 butonlu başarı dialogunu gösterir - DDL dosyalarını indirmek için
     */
    private void showSuccessDialogWithDownloadButtons(String schema, String tableName, String triggerName, 
                                                       String hisTableName, String seqName) {
        // Özel dialog oluştur
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Başarılı");
        
        // Header'ı moda göre ayarla
        if (scriptOnlyRadioButton.isSelected()) {
            dialog.setHeaderText("Script'ler başarıyla oluşturuldu!");
        } else {
            dialog.setHeaderText("Trigger ve History tablosu başarıyla oluşturuldu!");
        }
        
        // İçerik
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        
        // Bilgi metni
        Label infoLabel = new Label(
            "Tablo: " + tableName + "\n" +
            "Trigger: " + triggerName + "\n" +
            "History Tablosu: " + hisTableName + "\n" +
            "Sequence: " + seqName
        );
        infoLabel.setStyle("-fx-font-size: 12px;");
        
        // Buton başlığı
        Label downloadLabel = new Label("DDL Dosyalarını İndir:");
        downloadLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        // Butonlar için grid
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setAlignment(Pos.CENTER);
        
        // 1. Ana Tablo DDL butonu
        Button tableButton = new Button("📋 Ana Tablo DDL");
        tableButton.setPrefWidth(150);
        tableButton.setPrefHeight(35);
        tableButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        tableButton.setOnAction(e -> {
            String ddl = triggerService.generateMainTableDdl(schema, tableName);
            saveToFile(tableName + "_TABLE.ddl", ddl, "Ana Tablo DDL");
        });
        
        // 2. HIS Tablosu DDL butonu
        Button hisButton = new Button("📜 HIS Tablosu DDL");
        hisButton.setPrefWidth(150);
        hisButton.setPrefHeight(35);
        hisButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        hisButton.setOnAction(e -> {
            String ddl = triggerService.generateHisTableDdl(schema, tableName);
            saveToFile(hisTableName + ".ddl", ddl, "History Tablosu DDL");
        });
        
        // 3. Trigger DDL butonu
        Button triggerButton = new Button("⚡ Trigger DDL");
        triggerButton.setPrefWidth(150);
        triggerButton.setPrefHeight(35);
        triggerButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        triggerButton.setOnAction(e -> {
            String ddl = triggerService.generateTriggerDdl(schema, tableName);
            saveToFile(triggerName + ".trg", ddl, "Trigger DDL");
        });
        
        // 4. Sequence DDL butonu
        Button seqButton = new Button("🔢 Sequence DDL");
        seqButton.setPrefWidth(150);
        seqButton.setPrefHeight(35);
        seqButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        seqButton.setOnAction(e -> {
            String ddl = triggerService.generateSequenceDdl(schema, tableName);
            saveToFile(seqName + ".ddl", ddl, "Sequence DDL");
        });
        
        // Hover efektleri
        addHoverEffect(tableButton, "#1976D2", "#2196F3");
        addHoverEffect(hisButton, "#388E3C", "#4CAF50");
        addHoverEffect(triggerButton, "#F57C00", "#FF9800");
        addHoverEffect(seqButton, "#7B1FA2", "#9C27B0");
        
        // Butonları grid'e ekle (2x2)
        buttonGrid.add(tableButton, 0, 0);
        buttonGrid.add(hisButton, 1, 0);
        buttonGrid.add(triggerButton, 0, 1);
        buttonGrid.add(seqButton, 1, 1);
        
        content.getChildren().addAll(infoLabel, new Separator(), downloadLabel, buttonGrid);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(400);
        
        dialog.showAndWait();
    }
    
    /**
     * Buton hover efekti ekler
     */
    private void addHoverEffect(Button button, String hoverColor, String normalColor) {
        String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + "; " + baseStyle));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + normalColor + "; " + baseStyle));
    }
    
    /**
     * DDL içeriğini dosyaya kaydeder (FileChooser ile)
     */
    private void saveToFile(String defaultFileName, String content, String description) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(description + " - Kaydet");
        fileChooser.setInitialFileName(defaultFileName);
        
        // Dosya filtresi
        FileChooser.ExtensionFilter ddlFilter = new FileChooser.ExtensionFilter("DDL Dosyası (*.ddl)", "*.ddl");
        FileChooser.ExtensionFilter trgFilter = new FileChooser.ExtensionFilter("Trigger Dosyası (*.trg)", "*.trg");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tüm Dosyalar (*.*)", "*.*");
        
        if (defaultFileName.endsWith(".trg")) {
            fileChooser.getExtensionFilters().addAll(trgFilter, ddlFilter, allFilter);
        } else {
            fileChooser.getExtensionFilters().addAll(ddlFilter, trgFilter, allFilter);
        }
        
        // Varsayılan dizin (Desktop)
        File initialDir = new File(System.getProperty("user.home") + "/Desktop");
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        
        File file = fileChooser.showSaveDialog(primaryStage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", 
                    description + " başarıyla kaydedildi:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Hata", 
                    "Dosya kaydedilemedi:\n" + e.getMessage());
            }
        }
    }

    /**
     * Alert dialog gösterir
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Seçilen tabloyu döndürür
     */
    public String getSelectedTable() {
        return tableComboBox.getValue();
    }

    /**
     * Schema adını döndürür
     */
    public String getSchemaName() {
        return schemaComboBox.getValue();
    }

    /**
     * Uygulamayı başlatır
     */
    public static void launchApp() {
        launch();
    }

    /**
     * Main metodu - standalone çalıştırma için
     */
    public static void main(String[] args) {
        launch(args);
    }
}
