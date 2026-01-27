package org.example.money_busters_springboot.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
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
import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;
import org.example.money_busters_springboot.service.TriggerService;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

public class TriggerCreationApp extends Application {

    private ComboBox<String> schemaComboBox;
    private ComboBox<String> tableComboBox;
    private RadioButton dbRadioButton;
    private RadioButton scriptOnlyRadioButton;
    private TriggerService triggerService;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // SERVİS BAĞLANTISI
        this.triggerService = MoneyBustersSpringBootApplication.getBean(TriggerService.class);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20, 30, 20, 30));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        HBox topBox = new HBox(); topBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Trigger Oluşturma");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        HBox radioBox = new HBox(10); radioBox.setAlignment(Pos.CENTER_RIGHT);
        ToggleGroup modeGroup = new ToggleGroup();
        dbRadioButton = new RadioButton("Database'e Kaydet"); dbRadioButton.setToggleGroup(modeGroup); dbRadioButton.setSelected(true);
        scriptOnlyRadioButton = new RadioButton("Sadece Script"); scriptOnlyRadioButton.setToggleGroup(modeGroup);
        radioBox.getChildren().addAll(dbRadioButton, scriptOnlyRadioButton);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        topBox.getChildren().addAll(titleLabel, radioBox);

        GridPane formGrid = new GridPane(); formGrid.setHgap(15); formGrid.setVgap(15);
        schemaComboBox = new ComboBox<>(); schemaComboBox.setPrefWidth(250);
        tableComboBox = new ComboBox<>(); tableComboBox.setPrefWidth(250);

        formGrid.add(new Label("Schema:"), 0, 0); formGrid.add(schemaComboBox, 1, 0);
        formGrid.add(new Label("Table:"), 0, 1); formGrid.add(tableComboBox, 1, 1);

        Button createBtn = new Button("Trigger Oluştur");
        createBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> handleCreateTrigger());

        Button refreshBtn = new Button("Yenile");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> loadSchemas());

        HBox btnBox = new HBox(15); btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(createBtn, refreshBtn);

        schemaComboBox.setOnAction(e -> loadTables(schemaComboBox.getValue()));

        mainLayout.getChildren().addAll(topBox, formGrid, btnBox);
        loadSchemas();

        primaryStage.setScene(new Scene(mainLayout, 450, 250));
        primaryStage.setTitle("Trigger Automation Pro");
        primaryStage.show();
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
            new Alert(Alert.AlertType.WARNING, "Lütfen şema ve tablo seçiniz.").show();
            return;
        }

        if (triggerService.checkIfAnyExists(s, t)) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Bu tablo için kayıtlar zaten var.\n\n" +
                            "• Veriler KORUNACAK\n" +
                            "• Scriptler GÜNCELLEME modunda hazırlanacak\n\n" +
                            "Devam etmek istiyor musunuz?", ButtonType.YES, ButtonType.NO);
            if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }

        try {
            Map<String, String> scripts = triggerService.processTriggerRequest(s, t, dbRadioButton.isSelected());
            showSuccess(s, t, scripts, triggerService.checkIfAnyExists(s, t));
            if (dbRadioButton.isSelected()) loadTables(s);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Hata: " + ex.getMessage()).show();
        }
    }

    private void showSuccess(String s, String t, Map<String, String> scripts, boolean exists) {
        Dialog<Void> d = new Dialog<>(); d.setTitle("Başarılı"); d.setHeaderText("Scriptler Hazır!");
        VBox v = new VBox(15); v.setPadding(new Insets(20)); v.setAlignment(Pos.CENTER);

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        g.add(btn("📋 Main", "#2196F3", t + ".ddl", scripts.get("main")), 0, 0);
        g.add(btn("📜 HIS", "#4CAF50", t + "_HIS.ddl", scripts.get("his")), 1, 0);
        g.add(btn("⚡ Trigger", "#FF9800", "TRG_" + t + ".trg", scripts.get("trigger")), 0, 1);
        g.add(btn("🔢 Seq", "#9C27B0", "SEQ_" + t + ".ddl", scripts.get("seq")), 1, 1);

        Label rbL = new Label("Geri Alma Scriptleri:"); rbL.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        GridPane rbG = new GridPane(); rbG.setHgap(10); rbG.setVgap(10);
        rbG.add(btn("🔄 TRG RB", "#D32F2F", "TRG_" + t + "_RB.ddl", scripts.get("rb_trg")), 0, 0);
        rbG.add(btn("🔄 HIS RB", "#D32F2F", t + "_HIS_RB.ddl", scripts.get("rb_his")), 1, 0);
        rbG.add(btn("🔄 SEQ RB", "#D32F2F", "SEQ_" + t + "_RB.ddl", scripts.get("rb_seq")), 0, 1);
        rbG.add(btn("🔄 MAIN RB", "#D32F2F", t + "_RB.ddl", scripts.get("rb_main")), 1, 1);

        v.getChildren().addAll(new Label(exists ? "Güncelleme Yapıldı" : "Sıfırdan Oluşturuldu"), g, new Separator(), rbL, rbG);
        d.getDialogPane().setContent(v); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); d.showAndWait();
    }

    private Button btn(String txt, String color, String fname, String content) {
        Button b = new Button(txt); b.setPrefWidth(120);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        b.setOnAction(e -> {
            FileChooser fc = new FileChooser(); fc.setInitialFileName(fname);
            File f = fc.showSaveDialog(primaryStage);
            if (f != null) {
                try (FileWriter fw = new FileWriter(f)) { fw.write(content); } catch (Exception ignored) {}
            }
        });
        return b;
    }

    // --- İŞTE EKSİK OLAN MAIN METODU BURADA ---
    public static void main(String[] args) {
        launch(args);
    }
}