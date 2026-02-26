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

### Focus Mode (Odak Modu) + Uygulama Engelleme

Bu projede **Focus Mode**, kullanıcı odak oturumu başlattığında seçtiği uygulamaları (Instagram, TikTok, YouTube vb.) açmaya çalışırsa ekrana **engel (overlay) ekranı** getirir.

- Engelleme mekanizması **Accessibility Service** ile ön plandaki uygulamayı algılar.
- Hangi uygulamaların engelleneceği **Yasaklı Uygulamalar** ekranından seçilir.
- Engel ekranı `BlockScreenActivity` ile gösterilir (motivasyonel UI + kalan süre).

> Not: Android güvenliği gereği Accessibility iznini uygulama içinden otomatik verdirme mümkün değildir. Kullanıcı, ayarlar ekranında ilgili servisi manuel olarak açmalıdır.

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

### Accessibility (Odak Engeli İçin Gerekli)

Odak modunda uygulama engellemenin çalışması için:

- Ayarlar > Erişilebilirlik (Accessibility)
- Uygulamalar/Servisler bölümünde **TaskReminder** servisini aç

Uygulama içinden:

- `Settings > Yasaklı Uygulamalar` ekranındaki **Accessibility** butonu mümkünse direkt servis detay sayfasını açar, olmazsa genel accessibility ayarına düşer.
- Aynı ekrandaki **Uygulama ayarları** butonu cihazın `Uygulama bilgisi (App Info)` sayfasını açar (pil, bildirim vb. izinler için).

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

- Focus Mode
  - `FocusAccessibilityService.kt`
    - Ön plandaki uygulamayı algılar
    - Odak aktif + uygulama yasaklı ise `BlockScreenActivity` açar
  - `FocusPreferences.kt`
    - DataStore ile `blocked_packages`, `focus_enabled`, `focus_end_epoch_millis` yönetimi
  - `BlockedAppsScreen.kt`
    - Yasaklı uygulama seçimi
    - Tanılama / test butonları
  - `BlockScreenActivity.kt`
    - Engel ekranı UI

## Test Senaryoları

- **Sayaç testi**
  - 1-2 dakika sayaç ayarla
  - Calendar ekranında geri sayımı kontrol et
  - Süre dolunca bildirim bekle

- **Tarih/saat testi**
  - 2-3 dakika sonrasına saat ayarla
  - Exact alarm izninin açık olduğundan emin ol

- **Focus Mode / Engelleme testi**
  - Settings > Yasaklı Uygulamalar ekranından Instagram/TikTok/YouTube seç
  - Aynı ekranda **Odak başlat (5dk)** ile odak oturumunu başlat
  - Durum satırında `Odak=Aktif` gör
  - Yasaklı uygulamayı açmayı dene, engel ekranı bekle

### Sorun Giderme (En Sık)

- **Engelleme hiç çalışmıyor**
  - `Yasaklı Uygulamalar` ekranında `Accessibility=Açık` olmalı
  - Aynı ekranda `Odak=Aktif` olmalı
  - `Odak ham değerleri: enabled=true end=... kalan=...s` gibi değer görmelisin

- **Yasaklı uygulamalar listede görünmüyor**
  - Android 11+ için paket görünürlüğü (package visibility) gereksinimi vardır.
  - Bu projede `AndroidManifest.xml` içinde launcher uygulamalar için `<queries>` tanımlıdır.

- **VSCode kullanıyorum, Android Studio Logcat yok**
  - ADB ile log alabilirsin:

```powershell
adb devices
adb logcat -s FocusAccessibility
```

> Not: `adb` komutunun çalışması için Android SDK platform-tools PATH'te olmalı.

## Yol Haritası (Opsiyonel)

- Room ile kalıcı görev kaydı
- Görev düzenleme
- Bildirim tıklayınca görev detayına yönlendirme
- Daha gelişmiş takvim görünümü (ay/hafta)
