package com.gmc.libs;

import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * True if s == null or s is empty string.
     **/
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String content) {

        if (isNullOrEmpty(content)) content = "";
        content = content.replaceAll("\r", "");
        content = content.replaceAll("\n", "<br/>");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(content);
        }
    }

    /**
     * Hà Nội => ha noi
     * Đồng Tháp => dong thap
     */
    public static String normalizer(String input) {
        try {
            String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d");
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNumberic(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean inArray(String[] arr, String check) {
        if (arr == null || arr.length == 0 || check == null) return false;
        for(String s: arr) {
            if (check.equals(s)) return true;
        }
        return false;
    }

    public static void underlineTextView(TextView textView, String[] text) {
        String textViewContent = textView.getText().toString();
        SpannableString underlineContent = new SpannableString(textViewContent);
        for(String s : text) {
            int startIdx = textViewContent.indexOf(s);
            if (startIdx >= 0) {
                underlineContent.setSpan(new UnderlineSpan(), startIdx, startIdx + s.length(), 0);
            }
        }
        textView.setText(underlineContent);
    }

    public static boolean validEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return  (email.trim().matches(emailPattern));
    }


}
