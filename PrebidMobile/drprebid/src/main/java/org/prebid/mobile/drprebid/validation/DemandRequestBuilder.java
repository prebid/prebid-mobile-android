package org.prebid.mobile.drprebid.validation;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.ServerRequestSettings;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DemandRequestBuilder {
    private static final String TAG = DemandRequestBuilder.class.getSimpleName();

    private final Context context;
    private final String configId;
    private final List<AdSize> adSizes;

    public DemandRequestBuilder(Context context, String configId, AdSize adSize) {
        this.context = context;
        this.configId = configId;
        this.adSizes = new ArrayList<>();
        this.adSizes.add(adSize);
    }

    public String buildRequest(List<AdUnit> adUnits, String accountId, boolean secureParams) {
        try {
            return openRtbRequestBody(adUnits, accountId, secureParams).toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    private JSONObject openRtbRequestBody(List<AdUnit> adUnits, String accountId, boolean secureParams) {
        JSONObject object = new JSONObject();

        try {
            object.put("id", UUID.randomUUID().toString());
            object.put("source", getOpenRtbSource());
            object.put("app", getOpenRtbApp(accountId));
            object.put("device", getOpenRtbDevice());

            Boolean isSubjectToGDPR = TargetingParams.isSubjectToGDPR();
            if (isSubjectToGDPR != null && TargetingParams.isSubjectToGDPR()) {
                object.put("regs", getOpenRtbRegs());
            }

            object.put("user", getOpenRtbUser());
            object.put("imp", getOpenRtbImpressions(adUnits, secureParams));
            object.put("ext", getOpenRtbRequestExtension(accountId));
        } catch (JSONException exception) {

        }

        return object;
    }

    private JSONObject getOpenRtbSource() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("tid", "123");
        return object;
    }

    private JSONObject getOpenRtbRequestExtension(String accountId) throws JSONException {
        JSONObject prebid = new JSONObject();

        prebid.put("targeting", new JSONObject());

        JSONObject storedRequest = new JSONObject();
        storedRequest.put("id", accountId);
        prebid.put("storedrequest", storedRequest);

        JSONObject cache = new JSONObject();
        cache.put("bids", new JSONObject());
        prebid.put("cache", cache);

        JSONObject object = new JSONObject();
        object.put("prebid", prebid);
        return object;
    }

    private JSONArray getOpenRtbImpressions(List<AdUnit> adUnits, boolean secureParams) throws JSONException {
        JSONArray imps = new JSONArray();

        for (AdUnit adUnit : adUnits) {
            JSONObject imp = new JSONObject();
            imp.put("id", UUID.randomUUID().toString());
            if (secureParams) {
                imp.put("secure", 1);
            }

            JSONArray sizes = new JSONArray();
            for (AdSize size : adSizes) {
                JSONObject sizeObject = new JSONObject();
                sizeObject.put("w", size.getWidth());
                sizeObject.put("h", size.getHeight());
                sizes.put(sizeObject);
            }

            if (adUnit instanceof InterstitialAdUnit) {
                imp.put("instl", 1);

                Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                JSONObject sizeObject = new JSONObject();
                sizeObject.put("w", size.x);
                sizeObject.put("h", size.y);
                sizes.put(sizeObject);
            }

            JSONObject formats = new JSONObject();
            formats.put("format", sizes);
            imp.put("banner", formats);

            JSONObject ext = new JSONObject();
            JSONObject prebid = new JSONObject();
            JSONObject storedRequest = new JSONObject();
            storedRequest.put("id", configId);
            prebid.put("storedrequest", storedRequest);
            ext.put("prebid", prebid);

            imp.put("ext", ext);

            imps.put(imp);
        }

        return imps;
    }

    private JSONObject getOpenRtbApp(String accountId) throws JSONException {
        JSONObject object = new JSONObject();

        object.put("bundle", TargetingParams.getBundleName());

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            object.put("ver", info.versionName);
        } catch (PackageManager.NameNotFoundException exception) {
            Log.e(TAG, exception.getMessage());
        }

        JSONObject publisher = new JSONObject();
        publisher.put("id", accountId);
        object.put("publisher", publisher);

        JSONObject ext = new JSONObject();
        JSONObject prebid = new JSONObject();
        prebid.put("version", "1.0");
        prebid.put("source", "prebid-mobile");
        ext.put("prebid", prebid);
        object.put("ext", ext);

        return object;
    }

    private JSONObject getOpenRtbDevice() throws JSONException {
        JSONObject object = new JSONObject();

        JSONObject geo = getOpenRtbGeo();
        if (geo != null) {
            object.put("geo", geo);
        }

        object.put("make", Build.BRAND);
        object.put("model", Build.MODEL);
        object.put("os", "Android");
        object.put("osv", Build.VERSION.RELEASE);
        object.put("ua", ServerRequestSettings.getUserAgent());

        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        object.put("h", size.y);
        object.put("w", size.x);

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null) {
            object.put("carrier", telephonyManager.getNetworkOperatorName());
        }

        object.put("connectiontype", 1);

        //TODO obtain the MCC-MNC correctly from the OS
        object.put("mccmnc", "310-005");

        //TODO Properly obtain limit tracking and advertising id from OS
        object.put("lmt", 0);
        object.put("ifa", "cab4e66e-51d3-44a0-9fc6-924cc818a138");

        object.put("devtime", System.currentTimeMillis());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        object.put("pxratio", displayMetrics.density);

        return object;
    }

    private JSONObject getOpenRtbGeo() {
        return null;
    }

    private JSONObject getOpenRtbRegs() throws JSONException {
        JSONObject object = new JSONObject();

        boolean gdpr = false;
        Boolean isSubjectToGDPR = TargetingParams.isSubjectToGDPR();
        if (isSubjectToGDPR != null && TargetingParams.isSubjectToGDPR()) {
            gdpr = true;
        }

        JSONObject ext = new JSONObject();
        ext.put("gdpr", gdpr ? 1 : 0);
        object.put("ext", ext);

        return object;
    }

    private JSONObject getOpenRtbUser() throws JSONException {
        JSONObject object = new JSONObject();

        int yearOfBirth = TargetingParams.getYearOfBirth();
        if (yearOfBirth > 0) {
            object.put("yob", yearOfBirth);
        }

        String gender;
        switch (TargetingParams.getGender()) {
            case MALE:
                gender = "M";
                break;
            case FEMALE:
                gender = "F";
                break;
            default:
                gender = "O";
        }

        object.put("gender", gender);


        Boolean isSubjectToGDPR = TargetingParams.isSubjectToGDPR();
        if (isSubjectToGDPR != null && TargetingParams.isSubjectToGDPR()) {
            String consentString = TargetingParams.getGDPRConsentString();

            if (!TextUtils.isEmpty(consentString)) {
                JSONObject ext = new JSONObject();
                ext.put("consent", consentString);
                object.put("ext", ext);
            }
        }

        return object;
    }

    public String getKeywords(Map<String, List<String>> keywords) {
        StringBuilder keywordString = new StringBuilder();

        for (String key : keywords.keySet()) {
            List<String> values = keywords.get(key);
            if (values != null) {
                for (String value : values) {
                    String keyvalue = "";

                    if (TextUtils.isEmpty(value)) {
                        keyvalue = key;
                    } else {
                        keyvalue = String.format(Locale.ENGLISH, "%s=%s", key, value);
                    }

                    if (keywordString.length() > 0) {
                        keywordString.append(",");
                    }

                    keywordString.append(keyvalue);
                }
            }
        }

        return keywordString.toString();
    }
}
