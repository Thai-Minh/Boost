package com.cleaning.boost.ibooster.utils;

public class Constants {

    public static final int RC_ACTION_USAGE_ACCESS_SETTINGS = 10001;
    public static final int RC_READ_WRITE_STORAGE = 10002;

    public static final int ALL_PACKAGE_SIZE_COMPLETED = 10003;
    public static final int FETCH_PACKAGE_SIZE_COMPLETED = 10004;
    public static final int CPU_TEMPERATURE = 10005;

    public static final String  EXTRA_ACTIVITY = "extra.activity";
    public static final String  ACTIVITY_POLICY = "activity.policy";
    public static final String  ACTIVITY_SETTING = "activity.setting";
    public static final String  ACTIVITY_PERMISSION = "activity.permission";
    public static final String  ACTIVITY_FEEDBACK = "activity.feedback";

    public static final String  NOTIFICATION_INTENT = "notification.intent";
    public static final String  NOTIFICATION_CHANNEL_ID = "BoosterID";
    public static final String  NOTIFICATION_CHANNEL_NAME = "Booster";
    public static final int     NOTIFICATION_JUNK_FILES_ID = 100001;
    public static final int     NOTIFICATION_BOOST_ID = 100002;
    public static final int     NOTIFICATION_BATTERY_LOW_ID = 100003;
    public static final int     NOTIFICATION_CPU_COOLER_ID = 100004;

    public static final char[] pkgs = {'c','o','m','.','c','l','e','a','n','i','n','g','.','b','o','o','s','t','.','i','b','o','o','s','t','e','r'};

    /*
    * temperature < 15oC is cold,
    * from 15 - 45 is OK,
    * from 45 - 50 is warning,
    * over 50 is overheating */
    public static final String[] HEALTHY_CPU_TEMP_STATUS = {
            "Cold", "OK", "Hot Warning", "Overheating"
    };
    public static final int HEALTHY_CPU_TEMPERATURE_LOW = 15;
    public static final int HEALTHY_CPU_TEMPERATURE_HIGH = 45;
    public static final int HEALTHY_CPU_TEMPERATURE_OVERHEATING = 50;

    public static final String[] READ_WRITE_STORAGE_PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    public static final long TIME_REPEAT_SCAN_CACHE = 5 * 60 * 1000; // 5 mins
    public static final long TIME_REPEAT_CHECK_RAM = 2 * 60 * 1000; // 2 min
    public static final long TIME_SEPARATOR_CHECK_RAM_3H = 25 * 60 * 60 * 100;   // 2.5h
    public static final long TIME_SEPARATOR_CLEANED_MAX = 3 * 60 * 60 * 1000;   // 3h

    public static final long TIME_SEPARATOR_CLEANED = 30 * 60 * 1000;   // 30'
    public static final long TIME_SEPARATOR_BOOSTED = 15 * 60 * 1000;   // 15'
    public static final long TIME_SEPARATOR_COOLING = 60 * 1000;   // 1'
    public static final long TIME_SEPARATOR_COOLED = 5 * 60 * 1000;   // 5'

    public static final String SHARED_PREF_NOTIFICATION_WHITE_LIST = "shared.pref.notification.white.list";
    public static final String SHARED_PREF_FIRST_RUN_TIME = "shared.pref.first.run.time";
    public static final String SHARED_PREF_CLEANED_TIME = "shared.pref.cleaned.time";
    public static final String SHARED_PREF_PHONE_BOOST_TIME = "shared.pref.phone.boost.time";
    public static final String SHARED_PREF_PHONE_COOL_DOWN_TIME = "shared.pref.phone.cool.down.time";
    public static final String SHARED_PREF_LAST_TIME_SCAN_CACHE = "shared.pref.last.time.scan.cache";
    public static final String SHARED_PREF_LAST_TIME_CHECK_RAM = "shared.pref.last.time.check.ram";

    public static final String SHARED_PREF_LANGUAGE = "shared.pref.language";
    public static final String[] LANGUAGES = {"en:English", "cs:Čeština", "da:Dansk ", "de:Deutsch", "es:Español",
            "fr:Français", "gu:भारत", "hr:Hrvatski ", "hu:Magyar", "in:Bahasa Indonesia", "it:Italiano", "ja:日本国",
            "ko:한국어", "ms:Malay", "nl:Nederlands", "pl:Polski", "pt:Português", "ru:Pусский язык",
            "sk:Slovenský", "sl:Slovenski", "th:ภาษาไทย,", "tr:Türkçe", "zh:汉语", "vi:Tiếng Việt"};

    public static final String[] EMAILS = {"gmcgroup.studio@gmail.com"};

    // 08 11 13 19 23 25
    // Admob
	/*
	TEST:
	    APP ID: ca-app-pub-3940256099942544~3347511713
	    BID: ca-app-pub-3940256099942544/6300978111
	    IID: ca-app-pub-3940256099942544/1033173712
	MINHTOAN:
        MyApp ID: ca-app-pub-2132773825623829~7412197722
        Banner Bottom : ca-app-pub-2132773825623829/1468292826
        Banner 300x250: ca-app-pub-2132773825623829/6046117498
	*/
    public static final String TEST_DEVICE      = "53F0385B-B844A2A92C03219DECDF94F9";
    public static final String AM_APP_ID        = "ca-app-pub-2132773825623829~7412197722";
    public static final String AM_BID           = "ca-app-pub-2132773825623829/1468292826";
    public static final String AM_BID_300x250   = "ca-app-pub-2132773825623829/6046117498";
    public static final String AM_IID           = "ca-app-pub-2132773825623829/9811844363";
    // FAN
    public static final String FAN_BID          = "1732457840223910_1732458830223811";
    public static final String FAN_BID_300x250  = "1732457840223910_1732460153557012";
    public static final String FAN_IID          = "1732457840223910_1757381787731515";

    public static final String NOTIFY_JUNK = "notify.junk";
    public static final String NOTIFY_BOOST = "notify.boost";
    public static final String NOTIFY_COOLER = "notify.cooler";

    public static final int NOTIFICATION_ID = 10000;

}
