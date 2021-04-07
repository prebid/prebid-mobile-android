package com.openx.apollo.utils.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.openx.apollo.R;
import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.parser.AdResponseParserVast;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.video.vast.VAST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.openx.apollo.sdk.ApolloSettings.AUTO_REFRESH_DELAY_MAX;
import static com.openx.apollo.sdk.ApolloSettings.AUTO_REFRESH_DELAY_MIN;

public final class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static final String VAST_REGEX = "<VAST version=\"[\\d\\D]*.[\\d\\D]*\">";

    public static float DENSITY;

    private static final String[] mRecognizedMraidActionPrefixes = new String[]{
        "tel:",
        "voicemail:",
        "sms:",
        "mailto:",
        "geo:",
        "google.streetview:",
        "market:"};

    private static final String[] mVideoContent = new String[]{"3gp", "mp4", "ts", "webm", "mkv"};

    private static String convertParamsToString(String url, String key, String value) {

        StringBuilder template = new StringBuilder();
        template.append(url);

        //If value is null, we won't append anything related to it (key or null value) into the url
        if (value == null || value.equals("")) {
            return template.toString();
        }
        try {
            if (!key.equals("af")) {
                value = value.replaceAll("\\s+", "");
                value = URLEncoder.encode(value, "utf-8");
                value = value.replace("+", "%20");
            }
        }
        catch (UnsupportedEncodingException e) {
            OXLog.error(TAG, e.getMessage());
        }
        template.append("&")
                .append(key)
                .append("=")
                .append(value);

        return template.toString();
    }

    public static boolean isMraidActionUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            for (String prefix : mRecognizedMraidActionPrefixes) {
                if (url.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isVideoContent(String type) {
        if (!TextUtils.isEmpty(type)) {
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
            if (!TextUtils.isEmpty(ext)) {
                for (String content : mVideoContent) {
                    if (ext.equalsIgnoreCase(content)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * This function generates a SHA1 byte[] from another byte[].
     *
     * @param bytes
     * @return
     */
    public static byte[] generateSHA1(byte[] bytes) {
        byte[] encryted = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(bytes);
            encryted = digest.digest();
        }
        catch (Exception e) {
            e.getStackTrace();
        }

        return encryted;
    }

    /**
     * Generate SHA1 for string expression
     *
     * @param exp is string expression
     * @return SHA1 code
     */
    public static String generateSHA1(String exp) {
        try {
            byte[] datos = generateSHA1(exp.getBytes());
            return byteArrayToHexString(datos);
        }
        catch (Exception e) {
            e.getStackTrace();
            return null;
        }
    }

    /**
     * This function encodes byte[] into a hex
     *
     * @param byteArray
     * @return
     */
    public static String byteArrayToHexString(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(byteArray.length * 2);
        for (byte b : byteArray) {
            int v = b & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * Md5.
     *
     * @param s the string for which MD5 hash will be generated
     * @return the MD5 hash
     */
    public static String md5(String s) {
        if (Utils.isNotBlank(s)) {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(s.getBytes());
                byte[] messageDigest = digest.digest();

                StringBuilder hexString = new StringBuilder();
                for (byte b : messageDigest) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }
        return "";
    }

    /**
     * Load JavaScript file from resources.
     *
     * @param res  the resource path
     * @param file the resource file name
     * @return the JavaScript file content
     */
    public static String loadStringFromFile(Resources res, int file) {

        String content;

        try {
            // Resources res = getResources();
            InputStream in_s = res.openRawResource(file);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            content = new String(b);
            in_s.close();
        }
        catch (Exception e) {

            content = "";
        }

        return content;
    }

    /**
     * Check that Android SDK version at least Kit Kat
     *
     * @return true if at least Kit Kat
     */
    public static boolean atLeastKitKat() {
        return osAtLeast(Build.VERSION_CODES.KITKAT);
    }

    public static boolean atLeastQ() {
        return osAtLeast(Build.VERSION_CODES.Q);
    }

    /**
     * Check that Android SDK version at least Ice Cream Sandwich.
     *
     * @return true if at least Ice Cream Sandwich
     */
    public static boolean atLeastICS() {
        return osAtLeast(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    /**
     * Check the state that device external storage is available.
     *
     * @return true if available and writeable
     */
    public static boolean isExternalStorageAvailable() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        }
        else {
            // Something else is wrong. It may be one of many other states, but
            // all we need to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    private static boolean osAtLeast(int requiredVersion) {
        return Build.VERSION.SDK_INT >= requiredVersion;
    }

    public static boolean isScreenVisible(final int visibility) {
        return visibility == View.VISIBLE;
    }

    public static boolean hasScreenVisibilityChanged(final int oldVisibility,
                                                     final int newVisibility) {
        return (isScreenVisible(oldVisibility) != isScreenVisible(newVisibility));
    }

    /**
     * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
     * <p>
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     * <p>
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     * not empty and not null and not whitespace
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Get subsection of JSONArray, starting from 'start' and has length 'length'
     */
    public static JSONArray subJsonArray(final JSONArray array, int start, int length) {
        int end = Math.min(start + length, array.length());
        JSONArray result = new JSONArray();
        for (int i = start; i < end; i++) {
            Object object = array.opt(i);
            if (object != null) {
                result.put(object);
            }
        }
        return result;
    }

    public static BaseNetworkTask.GetUrlParams parseUrl(String url) {
        if (isBlank(url)) {
            return null;
        }

        try {
            URL urlObject = new URL(url);

            BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
            params.url = urlObject.getProtocol() + "://" + urlObject.getAuthority() + urlObject.getPath();
            params.queryParams = urlObject.getQuery();
            return params;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static int getScreenWidth(WindowManager windowManager) {
        if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                windowManager.getDefaultDisplay().getRealSize(size);
                return size.x;
            }
            else {
                DisplayMetrics metrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(metrics);
                return metrics.widthPixels;
            }
        }

        return 0;
    }

    public static int getScreenHeight(WindowManager windowManager) {
        if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                windowManager.getDefaultDisplay().getRealSize(size);
                return size.y;
            }
            else {
                DisplayMetrics metrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(metrics);
                return metrics.heightPixels;
            }
        }

        return 0;
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }

        return map;
    }

    public static View createCloseView(Context context) {
        if (context == null) {
            OXLog.error(TAG, "Unable to create close view. Context is null");
            return null;
        }

        View closeView = LayoutInflater.from(context).inflate(R.layout.lyt_close, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        closeView.setLayoutParams(params);
        return closeView;
    }

    public static View createWatchAgainView(Context context) {
        if (context == null) {
            OXLog.error(TAG, "Unable to create watch again view. Context is null");
            return null;
        }
        View watchAgainView = LayoutInflater.from(context).inflate(R.layout.lyt_watch_again, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        watchAgainView.setLayoutParams(params);
        return watchAgainView;
    }

    /**
     * Checks if the permission was granted
     *
     * @param context    - Activity context
     * @param permission - permission to check
     * @return
     */
    public static boolean isPermissionGranted(final Context context,
                                              final String permission) {
        if (context == null || permission == null) {
            OXLog.debug("Utils", "isPermissionGranted: Context or Permission is null");
            return false;
        }
        // Bug in ContextCompat where it can return a RuntimeException in rare circumstances.
        // If this happens, then we return false.
        try {
            return ContextCompat.checkSelfPermission(context, permission) ==
                   PackageManager.PERMISSION_GRANTED;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * @param durationInString String time in hh:mm:ss format
     * @return time converted to milliseconds, -1 if failed to convert
     */
    public static long getMsFrom(String durationInString) {
        if (TextUtils.isEmpty(durationInString)) {
            return -1;
        }
        long miliseconds = 0;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = dateFormat.parse(durationInString);
        }
        catch (ParseException e) {
            OXLog.error(TAG, "Unable to convert the videoDuration into seconds: " + e.getMessage());
        }
        if (date != null) {
            miliseconds = date.getTime();
        }
        return miliseconds;
    }

    /**
     * @param seconds
     * @return milliseconds value
     */
    public static long getMsFromSeconds(long seconds) {
        return seconds * 1000;
    }

    /**
     * @param data which is used to create {@link VAST} with the help of {@link AdResponseParserVast}
     * @return true if {@link VAST} creation was successful, false otherwise
     */
    public static boolean isVast(String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }

        Pattern pattern = Pattern.compile(VAST_REGEX);
        Matcher matcher = pattern.matcher(data);

        return matcher.find();
    }

    @NonNull
    public static String getFileExtension(String url) {
        String emptyExtension = "";
        if (url == null) {
            return emptyExtension;
        }

        String lastPathSegment = Uri.parse(url).getLastPathSegment();
        if (lastPathSegment == null) {
            return emptyExtension;
        }

        final int index = lastPathSegment.lastIndexOf(".");
        if (index == -1) {
            return emptyExtension;
        }

        return lastPathSegment.substring(index);
    }

    public static int clampAutoRefresh(int refreshDelay) {
        final int userRefreshValue = (int) getMsFromSeconds(refreshDelay);
        final int clampedRefreshInterval = clampInMillis(userRefreshValue, AUTO_REFRESH_DELAY_MIN, AUTO_REFRESH_DELAY_MAX);

        if (userRefreshValue < AUTO_REFRESH_DELAY_MIN || userRefreshValue > AUTO_REFRESH_DELAY_MAX) {
            OXLog.warn(TAG, "Refresh interval is out of range. Value which will be used for refresh: " + clampedRefreshInterval + ". "
                            + "Make sure that the refresh interval is in the following range: [" + AUTO_REFRESH_DELAY_MIN + ", " + AUTO_REFRESH_DELAY_MAX + "]");
        }

        return clampedRefreshInterval;
    }

    public static int clampInMillis(int value, int lowerBound, int upperBound) {
        return Math.min(Math.max(value, lowerBound), upperBound);
    }

    public static int generateRandomInt() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

    public static <E, U> JSONObject toJson(Map<E, ? extends Collection<U>> map) {
        JSONObject jsonObject = new JSONObject();
        if (map == null) {
            return jsonObject;
        }

        for (Map.Entry<E, ? extends Collection<U>> entry : map.entrySet()) {
            try {
                jsonObject.put(entry.getKey().toString(), new JSONArray(entry.getValue()));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    public static <E, U> void addValue(Map<E, Set<U>> map, E key, U value) {
        Set<U> valueSet = map.get(key);

        if (valueSet == null) {
            valueSet = new HashSet<>();
            map.put(key, valueSet);
        }

        valueSet.add(value);
    }

    public static void addValue(JSONObject target, String key, Object jsonObject) {
        try {
            target.put(key, jsonObject);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
