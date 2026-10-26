# Climora geliştirme notları

## Yapı
Uygulama başlangıcında `ClimoraApplication` tek bir `AppContainer` oluşturur. Şehir, tahmin, sohbet, fotoğraf ve konum servisleri buradan ViewModel fabrikalarına verilir. `LocationViewModel` tek seferlik navigasyon sonucunu tüketir; `CityPhotoViewModel` aynı ekranda kullanılan fotoğraf durumunu paylaşır. Görseller için 32 MB HTTP önbelleği ve sınırlı bellek önbelleği bulunur.

`GeoPoint` platformdan bağımsız uzaklık hesabıdır. Fotoğrafın şehirle eşleşmesi bu hesapla sınırlandırılır. Yerel görseller ve doğrulanmış çevrimiçi katalog önceliklidir; ardından çevrimiçi keşif gelir. Servislerden alınan hata gövdeleri kullanıcıya veya loga aktarılmaz.

## Yapılandırma
Yerel geliştirici ayarları `local.properties` üzerinden derlemede okunur. Bu dosya Git tarafından yok sayılır. Uygulama kullanıcısının bağlantı ayarı girmesi gerekmez. Halka açık dağıtımda gizli servis bilgilerinin uygulamaya gömülmesi yerine bir sunucu üzerinden erişim sağlanmalıdır.

## Kontrol ortamı
Java 21 kullanıldı. Gradle test işçisinin Türkçe kullanıcı klasöründeki classpath sorunu nedeniyle derlenmiş testler doğrudan JUnit ile çalıştırıldı. 54 birim testi geçti. Debug APK emülatörde kontrol edildi. Gemini'ye yapılan canlı deneme başarılı yanıt verdi.

Bu oturumda Java'nın dosya yazılabilirliği ön kontrolü, dosya yazılabilen klasörlerde bile olumsuz sonuç verebiliyor. Lint'in rapor oluşturması bu nedenle engellenmişti. Lint kuralları kapatılmadı; tamamlanamayan lint/release işlemi başarılı olarak değerlendirilmemelidir. Android Studio Terminal'inde Java 21 ile `./gradlew.bat lintDebug assembleRelease` ayrıca çalıştırılabilir.

## Fotoğraf kapsamı
55 yerel şehir fotoğrafı ve 68 doğrulanmış çevrimiçi kayıt bulunur. Çevrimiçi kayıtların 40'ı yurt dışındaki şehirlerdir; 28 Türkiye kaydı yerel katalogla kısmen örtüşür. Ad, koordinat, görsel adresi ve serbest lisans bilgileri denetlendi. Bu liste dünyanın tüm şehirlerini temsil etmez; servisteki silinme, bağlantı veya istek sınırı nedeniyle bir görsel geçici olarak gelmeyebilir.

Fotoğrafların eser sahibi/lisans/özgün dosya bağlantıları yerel JSON kataloglarında ve uygulamanın bilgi ekranında tutulur. Yerel fotoğraflar kırpılarak gösterilir. Kullanılan çevrimiçi görsel kataloğu `app/src/main/assets/online-city-photos.json` dosyasındadır.

68 çevrimiçi görsel adresinin tamamı HTTP 200 ve görsel içerik türüyle yanıt verdi. Bu erişim denetimi 26 Ekim 2026 tarihinde yapıldı; gelecekteki servis erişiminin garantisi değildir.
