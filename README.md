<p align="center">
  <img src="app/src/main/res/drawable-nodpi/climora_logo.png" alt="Climora logosu" width="220">
</p>

<h1 align="center">Climora · Hava ve Stil</h1>
<p align="center">Hava durumunu keşfet, gününe uygun giyin.</p>
<p align="center">Kotlin · Jetpack Compose · Android 8.0+</p>

## Proje hakkında

Climora'yı, hava durumuna bakarken günlük planımı ve ne giyeceğimi aynı yerden düşünebilmek için geliştirdim. Bilişim kampında hazırladığımız hava durumu uygulamasını temel alarak şehir fotoğrafları, ayrıntılı tahminler, konum desteği ve hava koşullarına göre öneri sunan bir giyim asistanı ekledim.

Arayüzde okunabilirliği ve kolay kullanımı; kod tarafında ise sorumlulukları ayrılmış, test edilebilir ve geliştirilebilir bir yapı kurmayı önceliklendirdim.

## Özellikler

| Alan | Sunulan özellikler |
| --- | --- |
| Şehir arama | Türkiye ve yurt dışındaki şehirleri arama ve seçme |
| Konum | Mevcut konumdan hava durumuna ulaşma, ilçe/şehir ve yaklaşık konum doğruluğunu görme |
| Tahminler | Anlık hava durumu, önümüzdeki 24 saat ve 7 / 15 günlük tahminler |
| Ayrıntılar | Hissedilen sıcaklık, nem, rüzgâr, yağış ihtimali, bulutluluk, gün doğumu ve gün batımı |
| Anlaşılır göstergeler | UV ve basınç değerlerini seviye açıklamalarıyla okuma |
| Şehir görselleri | Fotoğraflı hava durumu ekranları, çevrimiçi görsel arama ve önbellekleme |
| Ne giysem? | Hava durumuna göre ilk öneriyi otomatik alma ve sohbet ederek öneriyi kişiselleştirme |

Giyim asistanına günlük planımı, tarzımı veya başka bir şehri yazabiliyorum. Başka bir şehir için öneri istediğimde o şehrin hava verisi sorgulanıyor. Sohbet servisine erişilemediğinde uygulama bunu açıklıyor ve hava verilerinden oluşturulan yerel öneriyi ayrıca gösteriyor.

## Kullandığım teknolojiler

- **Dil ve arayüz:** Kotlin, Jetpack Compose, Material 3
- **Ekran yönetimi:** ViewModel, StateFlow, Navigation Compose
- **Asenkron işlemler:** Kotlin Coroutines
- **Veri erişimi:** Retrofit, OkHttp, Gson
- **Servisler:** Open-Meteo, Gemini, Wikipedia / Wikimedia
- **Test ve biçim:** JUnit, MockWebServer, Coroutines Test, Spotless

## Mimari

Arayüzü, ekran durumunu ve veri erişimini ayrı sorumluluklar olarak düzenledim. Ekranlar durumlarını ViewModel üzerinden alıyor; servisler `AppContainer` içinde oluşturulup bağımlılık olarak veriliyor. Böylece testlerde gerçek servisler yerine sahte uygulamalar kullanabiliyorum.

```text
app/src/main/
├── java/com/kampplus/hava/
│   ├── data/          # Servisler, repository'ler, konum ve önbellek
│   ├── model/         # Şehir, tahmin ve giyim önerisi modelleri
│   └── ui/
│       ├── city/      # Şehir arama ve konum
│       ├── forecast/  # Hava durumu ve tahminler
│       ├── chat/      # Giyim asistanı
│       ├── component/ # Ortak arayüz bileşenleri
│       └── theme/     # Tema, renkler ve yazı stilleri
├── assets/            # Fotoğraf katalogları ve lisans bilgileri
└── res/               # Görseller, simgeler ve Android kaynakları
```

## Kurulum ve çalıştırma

**Gereksinimler:** Android Studio, JDK 21, Android SDK 36 ve Android 8.0 (API 26) veya üzeri bir cihaz/emülatör.

1. Repoyu klonla:

   ```bash
   git clone https://github.com/elifnurtekay/Bilisim_Kampi_Hava-Durumu-Uygulamas-climora-.git
   ```

2. Proje klasörünü Android Studio ile aç ve Gradle eşitlemesini tamamla.
3. Bir cihaz veya emülatör seçip `app` modülünü çalıştır.

Windows terminalinden debug derlemesi almak için:

```powershell
.\gradlew.bat assembleDebug
```

macOS / Linux üzerinde `./gradlew assembleDebug` komutunu kullanabilirsin. Çevrimiçi hava verileri ve fotoğraflar internet bağlantısı gerektiriyor. Konum izni isteğe bağlı; izin vermeden de şehir araması yapılabiliyor. Sohbet servisi geliştirici yapılandırmasına bağlı; yapılandırılmadığında yerel giyim önerileri kullanılabiliyor.

## Testler

Testlerde servis yanıtlarının dönüşümünü, ekran durumlarını, iptal edilen istekleri, sohbet geçmişini, otomatik ilk öneriyi, başka şehir sorgusunu, konum sonucunu ve UV/basınç sınırlarını kontrol ediyorum.

```powershell
.\gradlew.bat testDebugUnitTest spotlessCheck lintDebug
```

GitHub Actions, `main` dalına gönderilen değişikliklerde ve pull request'lerde biçim kontrolünü, birim testlerini ve debug derlemesini çalıştırıyor. Yerel doğrulamada 54 birim testi geçti. Kontrol ortamına ilişkin ayrıntıları [geliştirme notlarında](docs/LOCAL_CUSTOMIZATION.md) tuttum.

## Veri ve görsel kaynakları

Hava tahminleri [Open-Meteo](https://open-meteo.com/) üzerinden alınıyor. Şehir fotoğrafları için yerel katalog ve [Wikimedia Commons](https://commons.wikimedia.org/) kaynaklarını kullanıyorum.

Uygulamada 55 şehir için yerel fotoğraf bulunuyor. Ek çevrimiçi katalogda Türkiye ve yurt dışından 68 kayıt var; bunların 40'ı yurt dışındaki şehirlerden oluşuyor. Katalogda olmayan yerlerde Wikipedia / Wikimedia üzerinden arama yapılıyor. Her yerleşim için uygun fotoğraf bulunamayabileceğinden gerektiğinde sade arka plan gösteriliyor.

Fotoğraflar canlı kamera görüntüsü değil. Eser sahibi, lisans ve özgün dosya bağlantıları kataloglarda ve uygulamanın bilgi alanlarında yer alıyor; görsellerin kullanım koşulları ilgili lisanslara tabi.

## Geliştirme süreci

Bu projeyi bilişim kampında öğrendiklerimi geliştirerek hazırladım. Kampın başlangıç yapısını ve ders notlarını [Kamp Notları](docs/KAMP_NOTLARI.md) dosyasında korudum. Climora'ya ait teknik kararları ve mevcut sınırlamaları [geliştirme notlarında](docs/LOCAL_CUSTOMIZATION.md) anlattım.

**Geliştiren:** [Elif Nur Tekay](https://github.com/elifnurtekay)
