package org.example.money_busters_springboot.ui;

import javafx.application.Application;
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
import javafx.stage.Stage;

import org.example.money_busters_springboot.config.DatabaseConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Trigger Oluşturma JavaFX Uygulaması
 */
public class TriggerCreationApp extends Application {

    // UI bileşenleri
    private TextField schemaTextField;
    private ComboBox<String> tableComboBox;
    private TextField triggerNameTextField;
    private Button createTriggerButton;
    private Button refreshButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Trigger Oluşturma");

        // Ana layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20, 30, 20, 30));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Başlık
        Label titleLabel = new Label("Trigger Oluşturma");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        // Form alanları için Grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        // Schema Label ve TextField
        Label schemaLabel = new Label("Schema:");
        schemaLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        schemaTextField = new TextField("UPT");
        schemaTextField.setPrefWidth(250);
        schemaTextField.setEditable(false);
        schemaTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 3;");

        // Table Label ve ComboBox
        Label tableLabel = new Label("Table:");
        tableLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        tableComboBox = new ComboBox<>();
        tableComboBox.setPrefWidth(250);
        tableComboBox.setPromptText("Tablo seçiniz...");
        tableComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 3;");

        // Trigger Name Label ve TextField
        Label triggerNameLabel = new Label("Trigger Name:");
        triggerNameLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        triggerNameTextField = new TextField();
        triggerNameTextField.setPrefWidth(250);
        triggerNameTextField.setPromptText("Örn: TRG_YENI_TRIGGER");
        triggerNameTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 3;");

        // Grid'e elemanları ekle
        formGrid.add(schemaLabel, 0, 0);
        formGrid.add(schemaTextField, 1, 0);
        formGrid.add(tableLabel, 0, 1);
        formGrid.add(tableComboBox, 1, 1);
        formGrid.add(triggerNameLabel, 0, 2);
        formGrid.add(triggerNameTextField, 1, 2);

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
        createTriggerButton.setOnMouseEntered(e -> 
            createTriggerButton.setStyle(
                "-fx-background-color: #45a049; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        createTriggerButton.setOnMouseExited(e -> 
            createTriggerButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );

        refreshButton.setOnMouseEntered(e -> 
            refreshButton.setStyle(
                "-fx-background-color: #1976D2; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        refreshButton.setOnMouseExited(e -> 
            refreshButton.setStyle(
                "-fx-background-color: #2196F3; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );

        // Event handlers
        refreshButton.setOnAction(e -> loadTables());
        
        createTriggerButton.setOnAction(e -> {
            // Arkadaşınız bu kısmı implement edecek
            String selectedTable = tableComboBox.getValue();
            String triggerName = triggerNameTextField.getText();
            
            if (selectedTable == null || selectedTable.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir tablo seçiniz.");
                return;
            }
            
            if (triggerName == null || triggerName.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen trigger adı giriniz.");
                return;
            }
            
            // TODO: Arkadaşınız burada trigger oluşturma ve history tablosu işlemlerini yapacak
            System.out.println("Trigger oluşturulacak: " + triggerName + " - Tablo: " + selectedTable);
            showAlert(Alert.AlertType.INFORMATION, "Bilgi", 
                "Trigger oluşturma işlemi başlatılacak.\n" +
                "Tablo: " + selectedTable + "\n" +
                "Trigger: " + triggerName);
        });

        // Ana layout'a ekle
        mainLayout.getChildren().addAll(titleLabel, formGrid, buttonBox);

        // Tabloları yükle
        loadTables();

        // Scene oluştur
        Scene scene = new Scene(mainLayout, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * UPT şemasındaki tabloları yükler (HIS içermeyen tablolar)
     */
    private void loadTables() {
        List<String> tables = new ArrayList<>();
        
        String sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = 'UPT' " +
                     "AND TABLE_NAME NOT LIKE '%HIS%' ORDER BY TABLE_NAME";
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfigLoader.getUrl(), 
                DatabaseConfigLoader.getUsername(), 
                DatabaseConfigLoader.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            
            ObservableList<String> tableList = FXCollections.observableArrayList(tables);
            tableComboBox.setItems(tableList);
            
            if (!tables.isEmpty()) {
                tableComboBox.setValue(tables.get(0));
            }
            
            System.out.println("Yüklenen tablo sayısı: " + tables.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Tablolar yüklenirken hata oluştu:\n" + e.getMessage());
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
     * Seçilen tabloyu döndürür - Arkadaşınız kullanabilir
     */
    public String getSelectedTable() {
        return tableComboBox.getValue();
    }

    /**
     * Girilen trigger adını döndürür - Arkadaşınız kullanabilir
     */
    public String getTriggerName() {
        return triggerNameTextField.getText();
    }

    /**
     * Schema adını döndürür - Arkadaşınız kullanabilir
     */
    public String getSchemaName() {
        return schemaTextField.getText();
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
