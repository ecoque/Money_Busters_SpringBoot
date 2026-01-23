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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.money_busters_springboot.config.DatabaseConfigLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Trigger Oluşturma JavaFX Uygulaması
 */
public class TriggerCreationApp extends Application {

    // UI bileşenleri
    private TextField schemaTextField;
    private ComboBox<String> tableComboBox;
    private Button createTriggerButton;
    private Button refreshButton;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
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

        // Grid'e elemanları ekle
        formGrid.add(schemaLabel, 0, 0);
        formGrid.add(schemaTextField, 1, 0);
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
            String selectedTable = tableComboBox.getValue();
            
            if (selectedTable == null || selectedTable.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir tablo seçiniz.");
                return;
            }
            
            // Otomatik isimler
            String schema = schemaTextField.getText();
            String triggerName = "TRG_" + selectedTable.toUpperCase();
            String hisTableName = selectedTable.toUpperCase() + "_HIS";
            String seqName = "SEQ_" + hisTableName;
            
            // Trigger, History tablosu ve Sequence oluştur
            try {
                // Önce kolonları al (DDL oluşturmak için lazım olacak)
                List<Map<String, Object>> columns = getTableColumnsForDdl(schema, selectedTable);
                
                createTriggerWithHistoryTable(schema, selectedTable, triggerName);
                
                // 4 butonlu özel başarı dialogunu göster
                showSuccessDialogWithDownloadButtons(schema, selectedTable, triggerName, hisTableName, seqName, columns);
                    
                // Tabloları yenile (artık HIS tablosu olduğu için listeden çıkacak)
                loadTables();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Hata", 
                    "Trigger oluşturulurken hata oluştu:\n" + ex.getMessage());
            }
        });

        // Ana layout'a ekle
        mainLayout.getChildren().addAll(titleLabel, formGrid, buttonBox);

        // Tabloları yükle
        loadTables();

        // Scene oluştur
        Scene scene = new Scene(mainLayout, 380, 200);
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
     * 4 butonlu başarı dialogunu gösterir - DDL dosyalarını indirmek için
     */
    private void showSuccessDialogWithDownloadButtons(String schema, String tableName, String triggerName, 
                                                       String hisTableName, String seqName, 
                                                       List<Map<String, Object>> columns) {
        // Özel dialog oluştur
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Başarılı");
        dialog.setHeaderText("Trigger ve History tablosu başarıyla oluşturuldu!");
        
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
            String ddl = generateMainTableDdl(schema, tableName, columns);
            saveToFile(tableName + "_TABLE.ddl", ddl, "Ana Tablo DDL");
        });
        
        // 2. HIS Tablosu DDL butonu
        Button hisButton = new Button("📜 HIS Tablosu DDL");
        hisButton.setPrefWidth(150);
        hisButton.setPrefHeight(35);
        hisButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        hisButton.setOnAction(e -> {
            String ddl = generateHisTableDdl(schema, tableName, columns);
            saveToFile(hisTableName + ".ddl", ddl, "History Tablosu DDL");
        });
        
        // 3. Trigger DDL butonu
        Button triggerButton = new Button("⚡ Trigger DDL");
        triggerButton.setPrefWidth(150);
        triggerButton.setPrefHeight(35);
        triggerButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        triggerButton.setOnAction(e -> {
            String ddl = generateTriggerSql(schema, tableName, triggerName, columns);
            saveToFile(triggerName + ".trg", ddl, "Trigger DDL");
        });
        
        // 4. Sequence DDL butonu
        Button seqButton = new Button("🔢 Sequence DDL");
        seqButton.setPrefWidth(150);
        seqButton.setPrefHeight(35);
        seqButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        seqButton.setOnAction(e -> {
            String ddl = generateSequenceDdl(schema, tableName);
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
     * Ana tablo için DDL oluşturur (CREATE TABLE ... AS SELECT ile)
     */
    private String generateMainTableDdl(String schema, String tableName, List<Map<String, Object>> columns) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("-- ").append(tableName).append(" Ana Tablo DDL\n");
        ddl.append("-- Oluşturulma Tarihi: ").append(java.time.LocalDateTime.now()).append("\n\n");
        
        ddl.append(String.format("CREATE TABLE %s.%s (\n", schema, tableName));
        
        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> col = columns.get(i);
            String colName = col.get("COLUMN_NAME").toString();
            String dataType = col.get("DATA_TYPE").toString();
            Object dataLength = col.get("DATA_LENGTH");
            Object precision = col.get("DATA_PRECISION");
            Object scale = col.get("DATA_SCALE");

            ddl.append("  ").append(colName).append(" ");

            if (dataType.contains("VARCHAR2") || dataType.contains("CHAR")) {
                ddl.append(dataType).append("(").append(dataLength).append(")");
            } else if (dataType.equals("NUMBER") && precision != null) {
                if (scale != null && ((Number)scale).intValue() > 0) {
                    ddl.append("NUMBER(").append(precision).append(",").append(scale).append(")");
                } else {
                    ddl.append("NUMBER(").append(precision).append(")");
                }
            } else {
                ddl.append(dataType);
            }
            
            if (i < columns.size() - 1) {
                ddl.append(",");
            }
            ddl.append("\n");
        }

        ddl.append(");\n");
        return ddl.toString();
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
     * DDL oluşturmak için tablo kolonlarını alır (bağlantı dışından)
     */
    private List<Map<String, Object>> getTableColumnsForDdl(String schema, String tableName) throws Exception {
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfigLoader.getUrl(),
                DatabaseConfigLoader.getUsername(),
                DatabaseConfigLoader.getPassword())) {
            return getTableColumns(conn, schema, tableName);
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
        return schemaTextField.getText();
    }

    /**
     * Trigger, History tablosu ve Sequence oluşturur
     * Varsa önce siler, sonra yeniden oluşturur
     */
    private void createTriggerWithHistoryTable(String schema, String tableName, String triggerName) throws Exception {
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfigLoader.getUrl(),
                DatabaseConfigLoader.getUsername(),
                DatabaseConfigLoader.getPassword())) {
            
            String hisTableName = tableName.toUpperCase() + "_HIS";
            String seqName = "SEQ_" + hisTableName;
            
            // 1. Varsa önce sil (sıra önemli: trigger -> tablo -> sequence)
            dropIfExists(conn, "TRIGGER", schema, triggerName);
            dropIfExists(conn, "TABLE", schema, hisTableName);
            dropIfExists(conn, "SEQUENCE", schema, seqName);
            
            // 2. Tablo kolonlarını al
            List<Map<String, Object>> columns = getTableColumns(conn, schema, tableName);
            
            if (columns.isEmpty()) {
                throw new Exception("Tablo kolonları bulunamadı: " + tableName);
            }
            
            // 3. History tablosu oluştur
            String hisTableDdl = generateHisTableDdl(schema, tableName, columns);
            System.out.println("History Table DDL:\n" + hisTableDdl);
            executeStatement(conn, hisTableDdl);
            System.out.println("History tablosu oluşturuldu: " + hisTableName);
            
            // 4. Sequence oluştur
            String seqDdl = generateSequenceDdl(schema, tableName);
            System.out.println("Sequence DDL:\n" + seqDdl);
            executeStatement(conn, seqDdl);
            System.out.println("Sequence oluşturuldu: " + seqName);
            
            // 5. Trigger oluştur
            String triggerSql = generateTriggerSql(schema, tableName, triggerName, columns);
            System.out.println("Trigger SQL:\n" + triggerSql);
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute(triggerSql);
            }
            System.out.println("Trigger oluşturuldu: " + triggerName);
        }
    }

    /**
     * Varsa nesneyi siler (TRIGGER, TABLE, SEQUENCE)
     */
    private void dropIfExists(Connection conn, String objectType, String schema, String objectName) {
        try {
            String dropSql = String.format("DROP %s %s.%s", objectType, schema, objectName);
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute(dropSql);
            }
            System.out.println(objectType + " silindi: " + objectName);
        } catch (Exception e) {
            // Nesne yoksa hata verir, görmezden gel
            System.out.println(objectType + " bulunamadı (yeni oluşturulacak): " + objectName);
        }
    }

    /**
     * Tek bir SQL statement çalıştırır
     */
    private void executeStatement(Connection conn, String sql) throws Exception {
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Tablo kolonlarını veritabanından alır
     */
    private List<Map<String, Object>> getTableColumns(Connection conn, String schema, String tableName) throws Exception {
        List<Map<String, Object>> columns = new ArrayList<>();
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE " +
                     "FROM ALL_TAB_COLUMNS " +
                     "WHERE OWNER = '" + schema.toUpperCase() + "' AND TABLE_NAME = '" + tableName.toUpperCase() + "' " +
                     "ORDER BY COLUMN_ID";
        
        try (java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> col = new java.util.LinkedHashMap<>();
                col.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                col.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                col.put("DATA_LENGTH", rs.getObject("DATA_LENGTH"));
                col.put("DATA_PRECISION", rs.getObject("DATA_PRECISION"));
                col.put("DATA_SCALE", rs.getObject("DATA_SCALE"));
                columns.add(col);
            }
        }
        System.out.println("Bulunan kolon sayısı: " + columns.size());
        return columns;
    }

    /**
     * Sequence DDL'ini oluşturur
     */
    private String generateSequenceDdl(String schema, String tableName) {
        String seqName = "SEQ_" + tableName.toUpperCase() + "_HIS";
        return String.format("CREATE SEQUENCE %s.%s START WITH 1 INCREMENT BY 1", schema, seqName);
    }

    /**
     * History tablosu DDL'ini oluşturur (Sequence hariç)
     */
    private String generateHisTableDdl(String schema, String tableName, List<Map<String, Object>> columns) {
        String hisTable = tableName.toUpperCase() + "_HIS";

        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format("CREATE TABLE %s.%s (", schema, hisTable));
        ddl.append("HIS_X NUMBER PRIMARY KEY, ");
        ddl.append("OP_TYPE CHAR(1), ");
        ddl.append("DML_USER VARCHAR2(100), ");
        ddl.append("DML_DATE DATE, ");

        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> col = columns.get(i);
            String colName = col.get("COLUMN_NAME").toString();
            String dataType = col.get("DATA_TYPE").toString();
            Object dataLength = col.get("DATA_LENGTH");
            Object precision = col.get("DATA_PRECISION");
            Object scale = col.get("DATA_SCALE");

            ddl.append(colName).append(" ");

            if (dataType.contains("VARCHAR2") || dataType.contains("CHAR")) {
                ddl.append(dataType).append("(").append(dataLength).append(")");
            } else if (dataType.equals("NUMBER") && precision != null) {
                if (scale != null && ((Number)scale).intValue() > 0) {
                    ddl.append("NUMBER(").append(precision).append(",").append(scale).append(")");
                } else {
                    ddl.append("NUMBER(").append(precision).append(")");
                }
            } else {
                ddl.append(dataType);
            }
            
            if (i < columns.size() - 1) {
                ddl.append(", ");
            }
        }

        ddl.append(")");
        return ddl.toString();
    }

    /**
     * Trigger SQL'ini oluşturur
     */
    private String generateTriggerSql(String schema, String tableName, String triggerName, List<Map<String, Object>> columns) {
        String hisTable = tableName.toUpperCase() + "_HIS";
        String sequenceName = "SEQ_" + hisTable;

        List<String> colNames = columns.stream()
                .map(c -> c.get("COLUMN_NAME").toString())
                .collect(java.util.stream.Collectors.toList());

        String colList = String.join(", ", colNames);
        String oldValues = colNames.stream().map(c -> ":OLD." + c).collect(java.util.stream.Collectors.joining(", "));
        String newValues = colNames.stream().map(c -> ":NEW." + c).collect(java.util.stream.Collectors.joining(", "));

        return String.format("""
            CREATE OR REPLACE TRIGGER %s.%s
            AFTER INSERT OR UPDATE OR DELETE ON %s.%s
            FOR EACH ROW
            DECLARE
                v_op_type CHAR(1);
            BEGIN
                IF INSERTING THEN
                    v_op_type := 'I';
                    INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.%s.NEXTVAL, v_op_type, SYS_CONTEXT('USERENV','SESSION_USER'), SYSDATE, %s);
                ELSIF UPDATING THEN
                    v_op_type := 'U';
                    INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.%s.NEXTVAL, v_op_type, SYS_CONTEXT('USERENV','SESSION_USER'), SYSDATE, %s);
                ELSIF DELETING THEN
                    v_op_type := 'D';
                    INSERT INTO %s.%s (HIS_X, OP_TYPE, DML_USER, DML_DATE, %s)
                    VALUES (%s.%s.NEXTVAL, v_op_type, SYS_CONTEXT('USERENV','SESSION_USER'), SYSDATE, %s);
                END IF;
            END;
            """,
                schema, triggerName, schema, tableName,
                schema, hisTable, colList, schema, sequenceName, newValues,
                schema, hisTable, colList, schema, sequenceName, newValues,
                schema, hisTable, colList, schema, sequenceName, oldValues
        );
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
