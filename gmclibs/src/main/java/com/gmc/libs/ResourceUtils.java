package com.gmc.libs;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresFeature;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResourceUtils {

    private static final String TAG = "ResourceUtils";
    private static String GMC_KEY_LANGUAGE = "gmc.key.language";
    private static String GMC_KEY_COUNTRY = "gmc.key.country";

    public static String getMetaDataString(Context context, String name) {
        String value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            LogUtils.e(TAG,"Couldn't find config value: " + name);
        }
        return value;
    }

    public static Integer getMetaDataInteger(Context context, String name) {
        Integer value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getInt(name);
        } catch (Exception e) {
            LogUtils.e(TAG,"Couldn't find config value: " + name);
        }

        return value;
    }

    public static Boolean getMetaDataBoolean(Context context, String name) {
        Boolean value = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            LogUtils.e(TAG,"Couldn't find config value: " + name);
        }

        return value;
    }

    public static String getContentResource(InputStream inputStream) {
        String val = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, Constants.Charset.UTF8);
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            val = sb.toString();
        } catch (Exception e) {
            LogUtils.e(TAG, "getContentResource" + e.toString());
        } finally {
            closeReader(bufferedReader);
            closeReader(inputStreamReader);
        }
        return val;
    }

    /**
     * fileName in assets folder:
     * fileName = folder/file.ext
     */
    public static String getContentAssetFile(Context context, String fileName) {
        String value = null;
        InputStream inputStream = null;
        try {
            AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open(fileName);
            value = getContentResource(inputStream);
        } catch (Exception e) {
            LogUtils.e(TAG, "getContentAssetFile" + e.getMessage());
        } finally {
            closeInputStream(inputStream);
        }
        return value;
    }

    public static Drawable getDrawableFromAsset(Context ctx, String path) {
        Drawable drawable = null;
        InputStream inputStream = null;
        try {
            inputStream = ctx.getAssets().open(path);
            drawable = Drawable.createFromStream(inputStream, null);
        } catch (Exception e) {
        } finally {
            closeInputStream(inputStream);
        }

        return drawable;
    }

    public static void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (Exception e) {
        }
    }

    public static void closeReader(InputStreamReader inputStreamReader) {
        try {
            if (inputStreamReader != null) inputStreamReader.close();
        } catch (Exception e) {
        }
    }

    public static void closeReader(BufferedReader bufferedReader) {
        try {
            if (bufferedReader != null) bufferedReader.close();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String content) {
        if (!StringUtils.isNullOrEmpty(content)) {
            content = content.replaceAll("\r", "");
            content = content.replaceAll("\n", "<br/>");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(content);
        }
    }

    /**
     * int[] sizes = {width, height};
     */
    public static int[] getScreenSize(Activity activity) {
        int[] sizes = {0, 0};
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        sizes[0] = displayMetrics.widthPixels;
        sizes[1] = displayMetrics.heightPixels;
        return sizes;
    }

    public static int pxToDp(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px,
                context.getResources().getDisplayMetrics());
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static int getColor(Context context, int colorResId) {
        return ContextCompat.getColor(context, colorResId);
    }

    public static Uri getUriForFile(Context context, File file) {
        if (Build.VERSION.SDK_INT < 24) {
            return Uri.fromFile(file);
        } else {
            // use this version for API >= 24
            return FileProvider.getUriForFile(context,
                    context.getApplicationInfo().packageName + ".fileprovider", file);
        }
    }

    public static void instalAPKFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getUriForFile(context, file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void downloadAndInstallApk(Context ctx, String url, String fileName, String title) {
        try {
            //notification(ctx, title, "Downloading " + title);
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/" + fileName;

            final Uri uri = Uri.parse("file://" + destination);

            System.out.println("Get " + url + " To " + destination);

            //set download manager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("Downloading " + title);
            request.setTitle(ctx.getResources().getString(R.string.app_name));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            request.setVisibleInDownloadsUi(true);

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctx, Intent intent) {
                    try {
                        if (TextUtils.equals(intent.getAction(), DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            install.setDataAndType(uri, "application/vnd.android.package-archive");
                            ctx.startActivity(install);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ctx.unregisterReceiver(this);
                }
            };
            //register receiver for when .apk download is compete
            ctx.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void refreshStorage(Context context, String mPath) {

        Uri contentUri = getUriForFile(context, new File(mPath));

        if (contentUri != null) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, contentUri));
            } else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(contentUri);
                context.sendBroadcast(mediaScanIntent);
            }
        }
    }

    public static boolean isPackageExisted(Context context, String targetPackage) {
        if (StringUtils.isNullOrEmpty(targetPackage)) return false;
        try {
            context.getPackageManager().getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * languageCode: en, vi,...
     * */
    public static void saveLanguage(Context context, String languageCode) {
        SharedPrefUtils.save(context, GMC_KEY_LANGUAGE, languageCode);
    }
    public static String getLanguage(Context context) {
        return SharedPrefUtils.getString(context, GMC_KEY_LANGUAGE, "");
    }

    /**
     * countryCode: us, vn, ...
     * */
    public static void saveCountry(Context context, String countryCode) {
        SharedPrefUtils.save(context, GMC_KEY_COUNTRY, countryCode);
    }
    public static String getCountry(Context context) {
        return SharedPrefUtils.getString(context, GMC_KEY_COUNTRY, "");
    }

    /**
     * In AndroidManifest.xml need add android:configChanges="locale" to activity
     * */
    @RequiresFeature(name = "AndroidManifest",
            enforcement = "In AndroidManifest.xml need add android:configChanges=\"locale\" to activity")
    public static void updateLanguage(Context context) {
        String language = getLanguage(context);
        String country = getCountry(context);
        if(StringUtils.isNullOrEmpty(language)) language = "en";

        Locale myLocale = null;
        if (StringUtils.isNullOrEmpty(country))
            myLocale = new Locale(language);
        else
            myLocale = new Locale(language, country);

        if (myLocale == null) return;

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static void deleteDir(File dir) {
        if (dir == null) return;
        if (dir.isDirectory()) {
            // if dir is folder
            String[] children = dir.list();
            // if folder has children, the first try delete all children
            if (children.length > 0) {
                for (int i = 0; i < children.length; i++) {
                    deleteDir(new File(dir, children[i]));
                }
            }
            // after that, delete itself
            dir.delete();
        } else {
            // if dir is file
            dir.delete();
        }
    }

    @NonNull
    public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static String getCountryCode(Context context) {
        return NetworkUtils.getCountryCode(context);
    }

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
