/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.utils.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.rendering.video.vast.VAST;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_MAX;
import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_MIN;

public final class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static final String VAST_REGEX = "<VAST\\s.*version\\s*=\\s*\".*\"(\\s.*|)?>";
    public static float DENSITY;

    private static final String[] recognizedMraidActionPrefixes = new String[]{"tel:", "voicemail:", "sms:", "mailto:", "geo:", "google.streetview:", "market:"};

    private static final String[] videoContent = new String[]{"3gp", "mp4", "ts", "webm", "mkv"};

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
            LogUtil.error(TAG, e.getMessage());
        }
        template.append("&")
                .append(key)
                .append("=")
                .append(value);

        return template.toString();
    }

    public static boolean isMraidActionUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            for (String prefix : recognizedMraidActionPrefixes) {
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
                for (String content : videoContent) {
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
     * Generate time-based UUID
     * @return  RFC 4122 high-quality random number
     */
    public static String generateUUIDTimeBased() {
        UUID timeUUID = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();
        timeUUID = new UUID(timeUUID.getMostSignificantBits(), timestamp);
        return timeUUID.toString();
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
        boolean externalStorageAvailable;
        boolean externalStorageWriteable;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        }
        else {
            // Something else is wrong. It may be one of many other states, but
            // all we need to know is we can neither read nor write
            externalStorageAvailable = externalStorageWriteable = false;
        }

        return externalStorageAvailable && externalStorageWriteable;
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

    public static View createSkipView(
            Context context,
            @Nullable InterstitialDisplayPropertiesInternal properties
    ) {
        return createButtonToNextPage(context,
                R.layout.lyt_skip,
                properties != null ? properties.skipButtonArea : 0,
                properties != null ? properties.skipButtonPosition : Position.TOP_RIGHT
        );
    }

    public static View createCloseView(Context context) {
        return createCloseView(context, null);
    }

    public static View createCloseView(
            Context context,
            @Nullable InterstitialDisplayPropertiesInternal properties
    ) {
        return createButtonToNextPage(context,
                R.layout.lyt_close,
                properties != null ? properties.closeButtonArea : 0,
                properties != null ? properties.closeButtonPosition : Position.TOP_RIGHT
        );
    }

    private static View createButtonToNextPage(
            @Nullable Context context,
            @LayoutRes int layoutResId,
            double buttonArea,
            @NonNull Position buttonPosition
    ) {
        if (context == null) {
            LogUtil.error(TAG, "Unable to create close view. Context is null");
            return null;
        }

        View view = LayoutInflater.from(context).inflate(layoutResId, null);

        FrameLayout.LayoutParams params;
        params = calculateButtonSize(view, buttonArea);
        params.gravity = Gravity.END | Gravity.TOP;
        if (buttonPosition == Position.TOP_LEFT) {
            params.gravity = Gravity.START | Gravity.TOP;
        }

        view.setLayoutParams(params);
        InsetsUtils.addCutoutAndNavigationInsets(view);
        return view;
    }

    private static final int MIN_BUTTON_SIZE_DP = 25;

    private static FrameLayout.LayoutParams calculateButtonSize(
            View view,
            double closeButtonArea
    ) {
        Context context = view.getContext();

        if (closeButtonArea < 0.05 || closeButtonArea > 1) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            return layoutParams;
        }

        int screenSize = getSmallestScreenSideSize(context);
        int buttonSize = (int) (screenSize * closeButtonArea);
        if (convertPxToDp(buttonSize, context) < MIN_BUTTON_SIZE_DP) {
            buttonSize = convertDpToPx(MIN_BUTTON_SIZE_DP, context);
        }
        int padding = (int) (buttonSize * 0.2);
        view.setPadding(padding, padding, padding, padding);
        return new FrameLayout.LayoutParams(buttonSize, buttonSize);
    }

    public static View createSoundView(Context context) {
        if (context == null) {
            LogUtil.error(TAG, "Unable to create view. Context is null");
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.lyt_sound, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.bottomMargin += 150;
        view.setLayoutParams(params);
        InsetsUtils.addCutoutAndNavigationInsets(view);
        return view;
    }

    private static int getSmallestScreenSideSize(Context context) {
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int result = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            if (result > 0) {
                return result;
            }
        } catch (Exception exception) {}
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels);
    }

    public static int convertPxToDp(
            int px,
            Context context
    ) {
        return (int) (px / ((float) context.getResources()
                                           .getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int convertDpToPx(
            int dp,
            Context context
    ) {
        return (int) (dp * ((float) context.getResources()
                                           .getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static View createWatchAgainView(Context context) {
        if (context == null) {
            LogUtil.error(TAG, "Unable to create watch again view. Context is null");
            return null;
        }
        View watchAgainView = LayoutInflater.from(context).inflate(R.layout.lyt_watch_again, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
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
            LogUtil.debug("Utils", "isPermissionGranted: Context or Permission is null");
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
            LogUtil.error(TAG, "Unable to convert the videoDuration into seconds: " + e.getMessage());
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
            LogUtil.error(TAG, "Refresh interval is out of range. Value which will be used for refresh: " + clampedRefreshInterval + ". "
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
