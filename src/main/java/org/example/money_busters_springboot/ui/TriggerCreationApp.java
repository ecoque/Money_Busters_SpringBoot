package org.example.money_busters_springboot.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;
import org.example.money_busters_springboot.service.TriggerService;
import org.example.money_busters_springboot.repository.TriggerRepository;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

public class TriggerCreationApp extends Application {

    private TriggerService triggerService;
    private TriggerRepository triggerRepository;
    private ComboBox<String> schemaComboBox = new ComboBox<>();
    private ComboBox<String> tableComboBox = new ComboBox<>();
    private RadioButton dbRadioButton = new RadioButton("Database'e Kaydet");
    private RadioButton scriptOnlyRadioButton = new RadioButton("Sadece Script");
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.triggerService = MoneyBustersSpringBootApplication.getBean(TriggerService.class);
        this.triggerRepository = MoneyBustersSpringBootApplication.getBean(TriggerRepository.class);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f8f9fa;");

        // Üst Kısım: Başlık ve Seçenekler (Görseldeki gibi yatay)
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Trigger Oluşturma");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        ToggleGroup modeGroup = new ToggleGroup();
        dbRadioButton.setToggleGroup(modeGroup);
        dbRadioButton.setSelected(true);
        scriptOnlyRadioButton.setToggleGroup(modeGroup);
        header.getChildren().addAll(title, dbRadioButton, scriptOnlyRadioButton);

        // Form: Schema ve Table
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(15);
        schemaComboBox.setPrefWidth(300);
        tableComboBox.setPrefWidth(300);
        form.add(new Label("Schema:"), 0, 0); form.add(schemaComboBox, 1, 0);
        form.add(new Label("Table:"), 0, 1); form.add(tableComboBox, 1, 1);

        // Butonlar
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        Button createBtn = new Button("Trigger Oluştur");
        createBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setPadding(new Insets(10, 20, 10, 20));

        Button refreshBtn = new Button("Yenile");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setPadding(new Insets(10, 25, 10, 25));

        buttons.getChildren().addAll(createBtn, refreshBtn);

        // Eventler
        refreshBtn.setOnAction(e -> {
            schemaComboBox.setItems(FXCollections.observableArrayList(triggerRepository.findAllSchemas()));
            schemaComboBox.setValue("UPT");
            loadTables("UPT");
        });
        schemaComboBox.setOnAction(e -> loadTables(schemaComboBox.getValue()));
        createBtn.setOnAction(e -> handleAction());

        layout.getChildren().addAll(header, form, buttons);

        // İlk yükleme
        schemaComboBox.setItems(FXCollections.observableArrayList(triggerRepository.findAllSchemas()));
        schemaComboBox.setValue("UPT");
        loadTables("UPT");

        primaryStage.setScene(new Scene(layout, 550, 300));
        primaryStage.show();
    }

    private void loadTables(String schema) {
        if (schema != null) {
            tableComboBox.setItems(FXCollections.observableArrayList(triggerRepository.findTablesBySchema(schema)));
        }
    }

    private void handleAction() {
        String s = schemaComboBox.getValue();
        String t = tableComboBox.getValue();
        if (s == null || t == null) return;

        try {
            Map<String, String> res = triggerService.processTriggerRequest(s, t, dbRadioButton.isSelected());
            showSuccess(t, res);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
        }
    }

    private void showSuccess(String t, Map<String, String> s) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Başarılı");
        d.setHeaderText(scriptOnlyRadioButton.isSelected() ? "Script'ler başarıyla oluşturuldu!" : "İşlem Tamamlandı!");

        VBox v = new VBox(15); v.setPadding(new Insets(20)); v.setAlignment(Pos.CENTER);
        v.getChildren().add(new Label("Tablo: " + t + "\nTrigger: TRG_" + t + "\nHistory: " + t + "_HIS"));

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setAlignment(Pos.CENTER);
        g.add(makeBtn("📋 Ana Tablo DDL", "#2196F3", e -> save(t + ".ddl", s.get("main"))), 0, 0);
        g.add(makeBtn("📜 HIS Tablosu DDL", "#4CAF50", e -> save(t + "_HIS.ddl", s.get("his"))), 1, 0);
        g.add(makeBtn("⚡ Trigger DDL", "#FF9800", e -> save("TRG_" + t + ".trg", s.get("trigger"))), 0, 1);
        g.add(makeBtn("🔢 Sequence DDL", "#9C27B0", e -> save("SEQ_" + t + ".ddl", s.get("seq"))), 1, 1);
        g.add(makeBtn("🔄 Rollback (RB.ddl)", "#F44336", e -> save(t + "_RB.ddl", s.get("rollback"))), 0, 2, 2, 1);

        v.getChildren().addAll(new Separator(), new Label("DDL Dosyalarını İndir:"), g);
        d.getDialogPane().setContent(v);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }

    private Button makeBtn(String txt, String clr, javafx.event.EventHandler<javafx.event.ActionEvent> ev) {
        Button b = new Button(txt); b.setPrefWidth(180);
        b.setStyle("-fx-background-color: " + clr + "; -fx-text-fill: white; -fx-font-weight: bold;");
        b.setOnAction(ev); return b;
    }

    private void save(String n, String c) {
        FileChooser fc = new FileChooser(); fc.setInitialFileName(n);
        File f = fc.showSaveDialog(primaryStage);
        if (f != null) {
            try (FileWriter fw = new FileWriter(f)) { fw.write(c); } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) { launch(args); }
}