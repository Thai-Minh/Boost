package com.gmc.libs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

public class NetworkUtils {

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean networkAvailable(Context context) {
        return isWifiConnection(context) || isMobileDataConnection(context);
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isMobileDataConnection(Context context) {
        //Check internet connection:
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Means that we are connected to a network (mobile or wi-fi)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                return nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

        }

        return false;
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isWifiConnection(Context context) {
        //Check internet connection:
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Means that we are connected to a network (mobile or wi-fi)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                return nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                || activeNetwork.getType() == ConnectivityManager.TYPE_WIMAX);

        }

        return false;
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivity.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String getContentFromUrl(String url) {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader bufferReader = null;
        StringBuffer sb = new StringBuffer();
        try {
            is = new URL(url).openStream();
            isr = new InputStreamReader(is, Constants.Charset.UTF8);
            bufferReader = new BufferedReader(isr);
            String text;
            while ((text = bufferReader.readLine()) != null) {
                sb.append(text).append("\n");
            }
        } catch (Exception e) {
        } finally {
            ResourceUtils.closeReader(bufferReader);
            ResourceUtils.closeReader(isr);
            ResourceUtils.closeInputStream(is);
        }
        return sb.toString();
    }

    public static String getIP4Address() {
        return getContentFromUrl("https://api.ipify.org");
    }

    static class GetLocationInfoFromIPAddressTask extends AsyncTask<Context, Void, JSONObject> {
        /*
        {   "city":"Hanoi",
            "country":"Vietnam",
            "countryCode":"VN",
            "isp":"FPTDYNAMICIP",
            "lat":21.0313,"lon":105.8516,
            "org":"FPT Telecom Company",
            "query":"113.23.55.141",
            "region":"HN","regionName":"Hanoi",
            "status":"success","timezone":"Asia/Ho_Chi_Minh","zip":"",
            "as":"AS18403 The Corporation for Financing \u0026 Promoting Technology"
        }
        */
        @Override
        protected JSONObject doInBackground(Context... params) {
            try {
                long t1 = System.currentTimeMillis();
                String content = getContentFromUrl("http://ip-api.com/json/" + getIP4Address());
                JSONObject js = new JSONObject(content);
                long t2 = System.currentTimeMillis();
                js.put("timeCost", (t2-t1));
                return js;
            } catch (Exception e) {
            }
            return null;
        }
    }

    public static JSONObject getLocationInfo() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new GetLocationInfoFromIPAddressTask().execute().get();
        } catch (Exception e) {}
        return jsonObject;
    }

    public static String getCountryCode(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
        } catch (Exception e) {
        }
        try {

            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }

        } catch (Exception e) {
        }
        try {
            // if can't get country from device, get country from ip network
            JSONObject locationInfo = getLocationInfo();
            if (locationInfo != null && locationInfo.has("countryCode")) {
                return locationInfo.getString("countryCode").toLowerCase(Locale.US);
            }

        } catch (Exception e) {
        }
        return "unknown";
    }

    public static double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;
    }

    public static double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

}
