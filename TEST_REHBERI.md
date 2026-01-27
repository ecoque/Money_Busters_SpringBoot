# 📚 Money Busters - Test Rehberi

## 🎯 Test Stratejisi

Bu proje için **4 farklı test türü** yazıldı:

### 1. **Unit Tests (Birim Testleri)** ⚙️
- Tek bir sınıfın/metodun izole olarak test edilmesi
- Bağımlılıklar mock/stub ile değiştirilir
- Hızlı çalışır, veritabanı gerektirmez

### 2. **Integration Tests (Entegrasyon Testleri)** 🔗
- Birden fazla komponentin birlikte çalışmasını test eder
- Gerçek veritabanı bağlantısı gerektirebilir
- Daha yavaş ama daha gerçekçi

### 3. **Controller Tests (API Testleri)** 🌐
- REST API endpoint'lerini test eder
- MockMvc kullanır
- HTTP request/response kontrolü

### 4. **Application Context Tests** 🏗️
- Spring Boot uygulamasının doğru başladığını kontrol eder
- Tüm bean'lerin yüklendiğini doğrular

---

## 📂 Yazılan Test Dosyaları

```
src/test/java/org/example/money_busters_springboot/
├── MoneyBustersSpringBootApplicationTests.java    ✅ Context testleri
├── controller/
│   └── TriggerControllerTest.java                 ✅ REST API testleri
├── service/
│   ├── TriggerGeneratorServiceTest.java           ✅ SQL generator testleri
│   └── TriggerServiceTest.java                    ✅ Trigger yönetim testleri
└── config/
    └── DatabaseConfigIntegrationTest.java         ✅ Veritabanı testleri
```

---

## 🧪 Test Detayları

### 1️⃣ TriggerGeneratorServiceTest
**Amaç**: SQL trigger üretimini test eder

**Test Senaryoları**:
- ✅ `testGenerateFullTriggerSql_BasariliSenaryo()` 
  - Trigger SQL'inin doğru üretilmesini kontrol eder
  - INSERT, UPDATE, DELETE kontrollerini doğrular
  
- ✅ `testGenerateFullTriggerSql_TekKolonIle()`
  - Tek kolonlu tablolar için SQL üretimini test eder
  
- ✅ `testGenerateFullTriggerSql_CokluKolonlar()`
  - Çok kolonlu tablolar için SQL üretimini test eder
  
- ✅ `testGenerateHistoryTableSql_BasariliSenaryo()`
  - History tablo SQL'inin doğru üretilmesini kontrol eder
  
- ✅ `testGenerateSequenceSql_DogruFormat()`
  - Sequence SQL'inin doğru formatını kontrol eder

**Nasıl Çalışır**:
```java
// Mock data oluşturulur
when(triggerRepository.getTableColumns(...)).thenReturn(mockColumns);

// Metod çağrılır
String sql = service.generateFullTriggerSql("UPT", "EMPLOYEES");

// Sonuç doğrulanır
assertTrue(sql.contains("CREATE OR REPLACE TRIGGER"));
```

---

### 2️⃣ TriggerServiceTest
**Amaç**: Trigger yönetim işlemlerini test eder

**Test Senaryoları**:
- ✅ `testGetAllTriggers_BasariliSenaryo()`
  - Tüm trigger'ların listelenmesini test eder
  
- ✅ `testGetTriggersByTable_BasariliSenaryo()`
  - Belirli bir tablonun trigger'larını getirir
  
- ✅ `testGetTriggerByName_TriggerBulundu()`
  - Trigger ismiye göre arama yapar
  
- ✅ `testGetTriggerByName_TriggerBulunamadi()`
  - Bulunamayan trigger için null dönmesini kontrol eder
  
- ✅ `testEnableTrigger_BasariliSenaryo()`
  - Trigger'ı aktif etme işlemini test eder
  
- ✅ `testDisableTrigger_BasariliSenaryo()`
  - Trigger'ı pasif etme işlemini test eder
  
- ✅ `testGetAllScripts_BasariliSenaryo()`
  - Tüm scriptlerin (trigger, table, sequence) alınmasını test eder

**Mock Kullanımı**:
```java
@Mock
private TriggerRepository triggerRepository;

@Mock
private JdbcTemplate jdbcTemplate;
```

---

### 3️⃣ TriggerControllerTest
**Amaç**: REST API endpoint'lerini test eder

**Test Senaryoları**:
- ✅ `testGetAllTriggers_Success()`
  - GET /api/triggers endpoint'ini test eder
  
- ✅ `testGetTriggersByTable_Success()`
  - GET /api/triggers/table/{tableName} endpoint'ini test eder
  
- ✅ `testGetTriggerByName_Found()`
  - GET /api/triggers/{triggerName} - başarılı senaryo
  
- ✅ `testGetTriggerByName_NotFound()`
  - GET /api/triggers/{triggerName} - 404 senaryosu
  
- ✅ `testEnableTrigger_Success()`
  - POST /api/triggers/{triggerName}/enable - başarılı
  
- ✅ `testEnableTrigger_Error()`
  - POST /api/triggers/{triggerName}/enable - hata senaryosu
  
- ✅ `testDisableTrigger_Success()`
  - POST /api/triggers/{triggerName}/disable
  
- ✅ `testGetScripts_Success()`
  - GET /api/triggers/generate-scripts/{tableName}
  
- ✅ `testCreateInsertTrigger_Success()`
  - POST /api/triggers/create/{tableName} - başarılı
  
- ✅ `testCreateInsertTrigger_Error()`
  - POST /api/triggers/create/{tableName} - hata senaryosu

**MockMvc Kullanımı**:
```java
mockMvc.perform(get("/api/triggers")
        .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.length()").value(2));
```

---

### 4️⃣ DatabaseConfigIntegrationTest
**Amaç**: Veritabanı bağlantısını test eder

**⚠️ DİKKAT**: Bu test **GERÇEK VERİTABANINA** bağlanır!

**Test Senaryoları**:
- ✅ `testDataSourceNotNull()` - DataSource bean kontrolü
- ✅ `testJdbcTemplateNotNull()` - JdbcTemplate bean kontrolü
- ✅ `testDatabaseConnection()` - Veritabanı bağlantısı kontrolü
- ✅ `testDatabaseVersion()` - Oracle versiyon kontrolü

---

### 5️⃣ MoneyBustersSpringBootApplicationTests
**Amaç**: Spring Boot Application Context testleri

**Test Senaryoları**:
- ✅ `contextLoads()` - Context yüklenme kontrolü
- ✅ `testTriggerServiceBeanExists()` - TriggerService bean kontrolü
- ✅ `testTriggerGeneratorServiceBeanExists()` - TriggerGeneratorService bean kontrolü
- ✅ `testAllRequiredBeansAreLoaded()` - Tüm bean'lerin toplu kontrolü

---

## 🚀 Testleri Nasıl Çalıştırırım?

### Tüm Testleri Çalıştır
```bash
mvn test
```

### Sadece Belirli Bir Test Sınıfını Çalıştır
```bash
mvn test -Dtest=TriggerGeneratorServiceTest
```

### Sadece Belirli Bir Test Metodunu Çalıştır
```bash
mvn test -Dtest=TriggerGeneratorServiceTest#testGenerateFullTriggerSql_BasariliSenaryo
```

### IntelliJ IDEA'da Çalıştır
1. Test dosyasını aç
2. Sınıf veya metod adının yanındaki yeşil ▶️ butonuna tıkla
3. "Run" seçeneğine tıkla

### Test Raporu Oluştur
```bash
mvn test
# Rapor: target/surefire-reports/ klasöründe
```

---

## 📊 Test Coverage (Kapsam Analizi)

### JaCoCo ile Coverage Raporu
POM.xml'e ekleyin:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Çalıştır:
```bash
mvn clean test jacoco:report
# Rapor: target/site/jacoco/index.html
```

---

## 🎓 Test Yazma Kılavuzu

### AAA Pattern (Arrange-Act-Assert)
```java
@Test
void testOrnek() {
    // Arrange (Hazırlık) - Test verilerini hazırla
    String schema = "UPT";
    String tableName = "EMPLOYEES";
    
    // Act (İşlem) - Test edilecek metodu çağır
    String result = service.generateSql(schema, tableName);
    
    // Assert (Doğrulama) - Sonucu kontrol et
    assertNotNull(result);
    assertTrue(result.contains("EMPLOYEES"));
}
```

### Mock Kullanımı
```java
@ExtendWith(MockitoExtension.class)
class MyTest {
    @Mock
    private MyRepository repository;
    
    @Test
    void test() {
        // Mock davranışını belirle
        when(repository.findById(1L)).thenReturn(mockData);
        
        // Test et
        MyObject result = service.getById(1L);
        
        // Doğrula
        verify(repository, times(1)).findById(1L);
    }
}
```

### Exception Test Etme
```java
@Test
void testException() {
    assertThrows(IllegalArgumentException.class, () -> {
        service.invalidMethod();
    });
}
```

---

## 🔍 Test Sonuçlarını Anlama

### ✅ Başarılı Test
```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

### ❌ Başarısız Test
```
[ERROR] testXXX  Time elapsed: 0.05 s  <<< FAILURE!
Expected: <5>
Actual: <3>
```

### ⏭️ Atlanan Test
```java
@Disabled("Henüz tamamlanmadı")
@Test
void testYapilacak() {
    // Test kodu
}
```

---

## 💡 İyi Pratikler

✅ **YAPILMASI GEREKENLER**:
- Her metod için en az bir test yaz
- Pozitif ve negatif senaryoları test et
- Test isimleri açıklayıcı olsun (`testGetAllTriggers_Success`)
- Mock'ları kullan, gerçek veritabanından kaçın (unit testlerde)
- Testler birbirinden bağımsız olmalı

❌ **YAPILMAMASI GEREKENLER**:
- Testlerde gerçek veritabanı değişikliği yapma
- Test sırasına bağımlı testler yazma
- Fazla karmaşık testler yazma
- Assertion olmayan testler yazma

---

## 🆘 Sık Karşılaşılan Sorunlar

### Problem: "Bean not found"
**Çözüm**: `@SpringBootTest` veya `@MockBean` ekle

### Problem: "Connection refused"
**Çözüm**: Veritabanı bağlantısı gerektiren testleri `@Disabled` ile devre dışı bırak

### Problem: "NullPointerException"
**Çözüm**: Mock davranışını `when(...).thenReturn(...)` ile tanımla

### Problem: Test çok yavaş
**Çözüm**: Integration testleri ayır, unit testlere odaklan

---

## 📈 Gelecek İyileştirmeler

Projeye eklenebilecek testler:
- [ ] Performance testleri (JMeter)
- [ ] Security testleri
- [ ] UI testleri (Selenium/JavaFX Robot)
- [ ] Load testleri
- [ ] Database rollback testleri

---

## 📞 Yardım

Test yazarken sorun yaşarsan:
1. Test metodunu debug modda çalıştır
2. Console çıktısını oku
3. Stack trace'i incele
4. Mock'ların doğru tanımlandığından emin ol

**Başarılar!** 🎉
