# Dreams Scanner

تطبيق Android أصلي (Kotlin + Jetpack Compose) لمسح المستندات وتحويلها إلى PDF. يعمل دون إنترنت، بلا إعلانات ولا اشتراكات ولا علامة مائية، ولا يجمع أي بيانات.

> **حالة المشروع: المرحلة 1 من 4.** الكود كُتب لكنه **لم يُبنَ ولم يُجرَّب على جهاز** بعد (بيئة الكتابة لا تملك Android SDK ولا وصولاً إلى مستودعات Maven). أول بناء قد يُظهر أخطاء صغيرة في الاستيراد أو الإصدارات؛ أرسلها لي وأصلحها قبل الانتقال للمرحلة التالية.

## خطة المراحل

| المرحلة | المحتوى | الحالة |
|---|---|---|
| 1 | الأساس: هيكل المشروع، الهوية والثيم (ليلي/نهاري)، عربي RTL/إنجليزي، قاعدة البيانات، الرئيسية، المجلدات، المفضلة، البحث، الإعدادات، استيراد صور من المعرض، عارض صفحات | مكتوبة، بانتظار أول بناء |
| 2 | الكاميرا (CameraX)، اكتشاف الحواف وتصحيح المنظور (OpenCV)، تعديل الزوايا يدوياً، تدوير، تصوير عدة صفحات، إعادة تصوير/حذف صفحة | لاحقاً |
| 3 | الفلاتر الستة، السطوع والتباين، إزالة الظلال، إنشاء PDF (A4/A5/Letter، الجودة، الضغط)، إعادة الترتيب بالسحب، المشاركة والحفظ والطباعة، معاينة PDF | لاحقاً |
| 4 | OCR عربي + إنجليزي (Tesseract بلغات مضمّنة)، نسخ النص وحفظ TXT، فهرسة النص للبحث | لاحقاً |

## ماذا يعمل في المرحلة 1

- الرئيسية: تبويبات المستندات / المجلدات / المفضلة، قسم «الأخيرة»، ترتيب بالتاريخ أو الاسم، بحث فوري في أسماء المستندات.
- استيراد صور من المعرض (اختيار متعدد، بدون أي صلاحية) لإنشاء مستند بصفحات متعددة.
- إنشاء المجلدات وإعادة تسميتها وحذفها (المستندات داخل المجلد المحذوف تبقى ولا تُحذف).
- لكل مستند: إعادة تسمية، نقل بين المجلدات، مفضلة، حذف نهائي مع تأكيد.
- عارض صفحات المستند بالسحب.
- الإعدادات: المظهر (حسب النظام / نهاري / ليلي)، اللغة (حسب النظام / العربية / English)، الخصوصية، حول التطبيق.
- لا يطلب التطبيق **أي صلاحية** في هذه المرحلة (لا إنترنت ولا تخزين). صلاحية الكاميرا فقط تُضاف في المرحلة 2.

## طريقة بناء APK

### الطريقة 1: Android Studio (الأسهل)
1. ثبّت Android Studio (الإصدار الأحدث المستقر).
2. `File > Open` واختر مجلد `DreamsScanner`.
3. انتظر `Gradle Sync` (يحتاج إنترنت أول مرة لتنزيل المكتبات).
4. وصّل هاتفك مع تفعيل «تصحيح USB» ثم اضغط Run، أو من القائمة: `Build > Build APK(s)`.
5. الملف الناتج: `app/build/outputs/apk/debug/app-debug.apk`.

### الطريقة 2: سطر الأوامر
يتطلب JDK 17 وAndroid SDK (متغير `ANDROID_HOME`):
```
./gradlew assembleRelease      # الناتج: app/build/outputs/apk/release/app-release.apk
./gradlew assembleDebug        # الناتج: app/build/outputs/apk/debug/app-debug.apk
```
نسخة release موقّعة بمفتاح debug كي تُثبَّت مباشرة للاستخدام الشخصي. للنشر العام أنشئ مفتاحك الخاص وعدّل `signingConfigs` في `app/build.gradle.kts`.

### الطريقة 3: بدون تثبيت أي شيء (GitHub Actions)
1. أنشئ مستودعاً على GitHub وارفع محتوى هذا المجلد.
2. من تبويب Actions شغّل `Build APK`.
3. حمّل الملف من Artifacts باسم `DreamsScanner-APK`.

### التثبيت على الهاتف
انسخ الـ APK إلى الهاتف وافتحه، وفعّل «التثبيت من مصادر غير معروفة» لتطبيق الملفات إذا طُلب.

## بنية المشروع

```
DreamsScanner/
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── gradle/libs.versions.toml        ← كل إصدارات المكتبات في مكان واحد
├── .github/workflows/build-apk.yml  ← بناء APK على GitHub
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/ (values, values-ar, values-night, drawable, mipmap-anydpi-v26, xml)
        └── java/com/dreams/scanner/
            ├── MainActivity.kt, AppContainer.kt (DreamsApp + حاوية الاعتمادات)
            ├── data/
            │   ├── db/ (Entities, Daos, AppDatabase)   ← Room
            │   ├── DocumentRepository.kt               ← كل عمليات المستندات والمجلدات والاستيراد
            │   ├── FileStore.kt                        ← تخزين الصور في مجلد التطبيق الخاص
            │   └── SettingsRepository.kt               ← DataStore (المظهر، الترتيب)
            └── ui/
                ├── theme/Theme.kt                      ← الألوان
                ├── components/Common.kt                ← صفوف وحوارات مشتركة
                ├── home/, folder/, document/, settings/
                ├── LibraryViewModel.kt                 ← سلوك مشترك (استيراد، إعادة تسمية، نقل، حذف)
                └── AppNavHost.kt                       ← التنقل بين الشاشات
```

## كيف تعدّل التطبيق مستقبلاً

- **الألوان:** `ui/theme/Theme.kt` (نهاري وليلي منفصلان).
- **النصوص والترجمة:** `res/values/strings.xml` (إنجليزي) و`res/values-ar/strings.xml` (عربي). أي نص جديد أضفه في الملفين.
- **أيقونة التطبيق:** `res/drawable/ic_launcher_foreground.xml` ولون الخلفية `ic_launcher_background` في `res/values/colors.xml`.
- **شاشة جديدة:** أنشئ `XxxScreen.kt` و`XxxViewModel.kt` وأضف مساراً في `ui/AppNavHost.kt`.
- **حقل جديد في قاعدة البيانات:** عدّل `Entities.kt` ثم ارفع `version` في `AppDatabase.kt` وأضف Migration (أو احذف بيانات التطبيق أثناء التطوير).
- **جودة صور الاستيراد:** الثوابت `MAX_SIDE` و`THUMB_SIDE` في `FileStore.kt`، وجودة JPEG في `DocumentRepository.importImages`.

## قرارات تقنية مهمة

- **OCR العربي:** مكتبة ML Kit لا تدعم العربية. سيُستخدم Tesseract (مثل `tesseract4android`) مع ملفي `ara.traineddata` و`eng.traineddata` مضمّنين في التطبيق كي يعمل دون إنترنت.
- **الحد الأدنى Android 10 (API 29)**، يعمل على Galaxy A05s. الصور الكبيرة تُصغَّر عند الاستيراد لحماية الذاكرة.
- **الخصوصية:** `allowBackup=false`، لا صلاحية INTERNET، والملفات في مجلد التطبيق الخاص.
- **الإصدارات:** AGP 8.7.3 / Kotlin 2.0.21 / Compose BOM 2024.10.01 / Room 2.6.1. تأكد عند أول مزامنة أنها تُحمَّل دون تحذيرات توافق.
