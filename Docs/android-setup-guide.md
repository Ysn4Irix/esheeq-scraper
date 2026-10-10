# دليل تشغيل وتثبيت تطبيق أندرويد الأصلي (Android Setup & Run Guide)

تطبيق **قصة عشق** الأصلي مبني بنسبة 100% باستخدام **Kotlin** و **Jetpack Compose** و **Jetpack Media3 (ExoPlayer)**.

---

## 1. المتطلبات الأساسية (Prerequisites)

- **Android Studio** (Koala / Ladybug أو أحدث) أو أداة `adb` سطرية.
- **Android SDK:** تم ضبط المشروع على `compileSdk = 35` و `minSdk = 26` (Android 8.0 Oreo+).
- **Java:** متوافق مع Java 17 و Java 21 LTS.
- **خادم الـ API المحلي:** Node.js (Express) يعمل على المنفذ `4000`.

---

## 2. تشغيل خادم الـ API الخلفي (Backend Server)

قبل تشغيل التطبيق على المحاكي أو الهاتف، تأكد من تشغيل خادم الـ API:

```bash
cd api
npm start
# يعمل الخادم على http://localhost:4000/api
```

> **ملاحظة حول الشبكة:**
> - داخل **محاكي أندرويد الرسمي (Android Emulator)**: يتصل التطبيق تلقائياً بالخادم عبر `http://10.0.2.2:4000/api/` (وهو عنوان loopback الخاص بنظام المضيف).
> - على **جهاز حقيقي عبر Wi-Fi**: تأكد من أن الهاتف وجهاز الكمبيوتر على نفس الشبكة، واستخدم عنوان IP الخاص بجهازك (مثل `http://192.168.1.50:4000/api/`).

---

## 3. تثبيت وتشغيل التطبيق عبر ADB

تم بناء حزمة الـ APK التجريبية بنجاح وهي متوفرة في المسار:
`android/app/build/outputs/apk/debug/app-debug.apk`

لتثبيتها مباشرة على أي جهاز أو محاكي متصل:

```powershell
adb install -r android\app\build\outputs\apk\debug\app-debug.apk
```

أو لتشغيل التطبيق مباشرة بعد التثبيت:

```powershell
adb shell am start -n com.esheeq.app/.MainActivity
```

---

## 4. الفتح والتطوير عبر Android Studio

1. افتح **Android Studio**.
2. اختر **Open** ثم حدد المجلد:
   `c:\Users\lenovo\Desktop\Projects\antigravity\esheeq-scraper\android`
3. سيقوم Android Studio بمزامنة ملفات Gradle تلقائياً (`Gradle Sync`).
4. اختر المحاكي أو جهازك الحقيقي واضغط على زر التشغيل الأخضر **Run (Shift + F10)**.

---

## 5. هيكلية الكود المصدري (Project Architecture)

- **`com.esheeq.app.MainActivity`**: نقطة الانطلاق الرئيسية مع دعم RTL وحواف الشاشة الكاملة (Edge-to-Edge).
- **`com.esheeq.app.ui.theme`**: ثيم الطرفية الداكن (Concept 2 Terminal Dark: `#0D1117`, `#161B22`, `#58A6FF`, `#2EA043`).
- **`com.esheeq.app.ui.components`**:
  - `TerminalTopBar`: الترويسة الطرفية مع مؤشر الحالة النابض.
  - `SearchBarComponent`: حقل البحث الطرفي مع دعم البحث الفوري.
  - `LatestEpisodeCard`: بطاقة الحلقة مع أزرار المشاهدة والنسخ.
  - `SeriesCard`: بطاقات نتائج البحث وفهرس الأعمال.
- **`com.esheeq.app.ui.home`**: الشاشة الرئيسية وعرض الحلقات والبحث الفوري.
- **`com.esheeq.app.ui.series`**: شاشة تفاصيل المسلسل، بيانات العمل، شبكة الحلقات، وزر الاستخراج الجماعي.
- **`com.esheeq.app.ui.player`**: شاشة المشغل الهجين المدمج (Media3 ExoPlayer + WebView Embed + Picture-in-Picture).
