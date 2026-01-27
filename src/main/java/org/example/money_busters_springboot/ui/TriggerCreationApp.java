package org.example.money_busters_springboot.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.money_busters_springboot.MoneyBustersSpringBootApplication;
import org.example.money_busters_springboot.service.TriggerService;
import org.example.money_busters_springboot.repository.TriggerRepository; // Geçici olarak tablo listesi için

import java.util.List;

public class TriggerCreationApp extends Application {

    // SPRING BOOT DERSİ 2: Dependency Injection (Bağımlılık Enjeksiyonu)
    // Bu nesneleri "new" yaparak oluşturmuyoruz. Spring zaten bunları arka planda oluşturdu.
    // Biz sadece "Spring kanka, bana oradan servisi ver" diyoruz.
    private TriggerService triggerService;
    private TriggerRepository triggerRepository;

    private ComboBox<String> schemaComboBox;
    private ComboBox<String> tableComboBox;
    private Button createTriggerButton;

    @Override
    public void start(Stage primaryStage) {
        // ADIM 3: Spring Context'ten Bean'leri çekiyoruz
        // JavaFX Spring dışında çalıştığı için bu köprüyü kurmak zorundayız.
        this.triggerService = MoneyBustersSpringBootApplication.getBean(TriggerService.class);
        this.triggerRepository = MoneyBustersSpringBootApplication.getBean(TriggerRepository.class);

        primaryStage.setTitle("Support Bridge - Trigger Automation");

        // Arayüz Tasarımı (Arkadaşının tasarımını sadeleştirdik ama mantık aynı)
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        schemaComboBox = new ComboBox<>();
        schemaComboBox.setPromptText("Şema Seçin");

        tableComboBox = new ComboBox<>();
        tableComboBox.setPromptText("Tablo Seçin");

        createTriggerButton = new Button("Trigger ve HIS Tablosu Oluştur");
        createTriggerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        // EVENT HANDLERS (Olay Yakalayıcılar)

        // 1. Şemaları Yükle
        loadSchemas();

        // 2. Şema seçilince tabloları getir
        schemaComboBox.setOnAction(e -> loadTables(schemaComboBox.getValue()));

        // 3. Butona basınca asıl işi servise devret
        createTriggerButton.setOnAction(e -> handleCreateTrigger());

        layout.getChildren().addAll(new Label("Schema:"), schemaComboBox, new Label("Table:"), tableComboBox, createTriggerButton);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // SPRING BOOT DERSİ 3: İş Mantığını Soyutlama
    // UI artık "SELECT * FROM ALL_TABLES" gibi SQL cümleleri bilmez.
    // Sadece repository'deki hazır metodları çağırır.
    private void loadSchemas() {
        List<String> schemas = triggerRepository.findAllSchemas();
        schemaComboBox.setItems(FXCollections.observableArrayList(schemas));
    }

    private void loadTables(String schema) {
        if (schema != null) {
            List<String> tables = triggerRepository.findTablesBySchema(schema);
            tableComboBox.setItems(FXCollections.observableArrayList(tables));
        }
    }

    private void handleCreateTrigger() {
        String schema = schemaComboBox.getValue();
        String table = tableComboBox.getValue();

        if (schema == null || table == null) {
            showAlert("Hata", "Lütfen şema ve tablo seçin!");
            return;
        }

        try {
            // ADIM 2'NİN EN ÖNEMLİ KISMI:
            // Burada 500 satır kod yazmak yerine, işi uzmanına (TriggerService) devrediyoruz.
            triggerService.createInsertTrigger(schema, table);

            showAlert("Başarılı", table + " için tarihçe tablosu ve trigger başarıyla veritabanına eklendi!");
        } catch (Exception ex) {
            showAlert("Hata", "İşlem başarısız: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // JavaFX uygulamasını başlatan kapı
    public static void main(String[] args) {
        launch(args);
    }


}