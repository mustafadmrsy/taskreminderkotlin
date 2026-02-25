# TaskReminder (Jetpack Compose)

Modern, sade ama "case" seviyesinde tasarlanmış bir **görev / hatırlatıcı** Android uygulaması.

- Material 3 (dark mode destekli)
- Jetpack Compose UI
- Navigation (Home / List / Calendar / Settings)
- ViewModel ile state yönetimi
- Zamanlı bildirim: **Exact Alarm (AlarmManager)** + fallback **WorkManager**
- Uygulama içi takvim görünümü (Calendar ekranı)

## Özellikler

### Ekranlar

- **Home**
  - Görev başlığı girme
  - 2 mod:
    - **Tarih + saat** ile planlı hatırlatma
    - **Sayaç (countdown)**: X dakika sonra hatırlatma
  - Seçenekler:
    - **Bildirimle hatırlat**

- **List**
  - Görevleri kartlı listeleme
  - Tamamlandı işaretleme

- **Calendar**
  - Gün seçimi (Material 3 DatePicker)
  - Seçilen güne ait görevleri kartlı gösterme
  - Sayaçla eklenen görevlerde **canlı geri sayım (mm:ss)**

- **Settings**
  - Tema: System / Light / Dark
  - Dynamic Color (Android 12+)

## Teknolojiler

- Kotlin
- Jetpack Compose + Material 3
- Navigation-Compose
- Lifecycle + ViewModel + StateFlow
- AlarmManager (setExactAndAllowWhileIdle)
- WorkManager (fallback)

## Kurulum

### Gereksinimler

- Android Studio (Giraffe ve üzeri önerilir)
- Android SDK
- Cihaz veya emulator

### Android SDK yolu (local.properties)

Bazı durumlarda Gradle şu hatayı verebilir:

`SDK location not found. Define a valid SDK location ...`

Bu durumda proje kök dizininde `local.properties` dosyası oluşturup Android SDK yolunu tanımlayın.

> Not: `local.properties` kullanıcı/makineye özel olduğu için genelde `.gitignore` içindedir ve repo'ya eklenmez.

Örnek (Windows):

```properties
sdk.dir=C\:\\Users\\<kullanici_adi>\\AppData\\Local\\Android\\Sdk
```

### Çalıştırma (Android Studio)

- Üst bardan **Run configuration: `app`** seç
- Cihazı seç
- **Run (▶)**

### Terminal ile Build / Kurulum

Proje kökünde:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

## İzinler ve Sistem Ayarları

### Bildirim izni (Android 13+)

Uygulama ilk açılışta `POST_NOTIFICATIONS` izni ister.

### Exact Alarm (Android 12+)

Planlı hatırlatmaların **tam saatinde** gelmesi için Android 12+ cihazlarda **Exact alarm** izni gerekebilir.

- Uygulama açılışta gerekli ekrana yönlendirir.
- Manuel yol:
  - Settings > Apps > Special access > Alarms & reminders (Exact alarms)
  - TaskReminder için izin ver

> Not: Exact alarm kapalıysa uygulama WorkManager ile fallback yapar; bu yöntem tam dakikasına garanti vermez.

### Takvim

Takvim sadece uygulamanın kendi içinde yer alan **Calendar** ekranıdır.

## Proje Yapısı (Özet)

- `MainActivity.kt`
  - Navigation host
  - Permission handling
  - UiEvent dinleme (reminder schedule)

- `MainViewModel.kt`
  - `StateFlow<MainState>`
  - Görev oluşturma / validasyon
  - `UiEvent` ile tek yönlü UI side-effect tetikleme

- UI
  - `HomeScreen.kt`
  - `ListScreen.kt`
  - `CalendarScreen.kt`
  - `SettingsScreen.kt`
  - `TaskCard.kt`

- Reminders
  - `AlarmScheduler.kt` (exact alarm)
  - `ReminderReceiver.kt` (alarm tetikleyince notification)
  - `ReminderScheduler.kt` + `ReminderWorker.kt` (WorkManager fallback)

## Test Senaryoları

- **Sayaç testi**
  - 1-2 dakika sayaç ayarla
  - Calendar ekranında geri sayımı kontrol et
  - Süre dolunca bildirim bekle

- **Tarih/saat testi**
  - 2-3 dakika sonrasına saat ayarla
  - Exact alarm izninin açık olduğundan emin ol

## Yol Haritası (Opsiyonel)

- Room ile kalıcı görev kaydı
- Görev düzenleme / silme
- Bildirim tıklayınca görev detayına yönlendirme
- Daha gelişmiş takvim görünümü (ay/hafta)

## Lisans

Henüz lisans eklenmedi. İstersen MIT ekleyebilirim.
