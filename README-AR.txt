مشروع روضة المسلم - قالب APK للبناء أونلاين عبر GitHub Actions

الموجود:
- ملف HTML الحالي داخل app/src/main/assets/www/index.html
- مشروع Android WebView أساسي.
- هيكل جدولة مواقيت الصلاة والتنبيه قبل 10 دقائق عبر AlarmManager.
- خدمة تشغيل أذان أصلية من ملفات MP3 داخل assets.
- إعادة الجدولة بعد إعادة تشغيل الجهاز أو تغيير الوقت/المنطقة الزمنية.
- GitHub Actions لبناء APK Debug ورفعه كـ Artifact.

مهم قبل اعتبار المشروع جاهزًا للاستخدام:
1) انسخ standard1-quran.pdf إلى app/src/main/assets/www/ حتى يعمل المصحف دون إنترنت.
2) انسخ ملفات MP3 التي يستخدمها HTML إلى app/src/main/assets/www/، وبالأسماء نفسها حرفيًا، ومنها:
   اذان الفجر.mp3
   اذان عبدالباسط.mp3
   اذان محمد رفعت.mp3
   اذان ناصر القطامي.mp3
   اذان مشاري بن راشد.mp3
   اذان محمود خليل الحصري.mp3
   اذان محمد علي البنا.mp3
   اذان مصطفي اسماعيل.mp3
3) يلزم ربط مزامنة إعدادات HTML بالجسر AndroidPrayer؛ أضف استدعاء المزامنة بعد تحميل الصفحة وبعد تغيير الإعدادات/المواقيت. مثال:
   function syncNativePrayerAlarms(){
     if(window.AndroidPrayer){
       AndroidPrayer.syncPrayerData(JSON.stringify(prayersList),
         document.getElementById('preAdhanAlertCheck').checked,
         document.getElementById('muteModeCheck').checked);
     }
   }
   استدعِ الدالة بعد renderPrayers()، وفي نهاية updatePrayerTime وupdateGlobalAdhan وsaveAdhanSettings.
   ويجب تهيئة checkboxes من localStorage قبل المزامنة.

ملاحظات مهمة:
- هذا مشروع بداية يحتاج اختبارًا فعليًا على أجهزة Android وإكمال مزامنة الإعدادات قبل التوزيع.
- Android 12+ قد يتطلب السماح بالمنبهات الدقيقة من إعدادات النظام.
- Android 13+ يتطلب إذن الإشعارات.
- وضع الصامت/عدم الإزعاج وإعدادات البطارية قد تمنع الصوت أو تؤخره. لا يمكن ضمان تجاوزها.
- إيقاف التطبيق إجباريًا من إعدادات النظام (Force stop) يمنع الاستقبال حتى فتحه مجددًا.
- ملف APK الناتج من workflow هو Debug للتجربة، وليس إصدارًا موقّعًا للنشر العام.

طريقة البناء:
1. أنشئ Repository جديدًا على GitHub باسم RawdatAlMuslim.
2. ارفع كل محتويات هذا المجلد إلى جذر المستودع.
3. افتح تبويب Actions، وفعّل workflows إذا طلب GitHub ذلك.
4. اختر Build Rawdat Al Muslim APK ثم Run workflow.
5. بعد نجاح البناء افتح العملية، وانزل إلى Artifacts، وحمّل RawdatAlMuslim-debug-apk.
