package org.prebid.mobile;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NativeAdUnit extends AdUnit{
    public enum NATIVE_REQUEST_VERSION{
        VERSION_1_1,
        VERSION_1_2
    }

    enum NATIVE_REQUEST_ASSET{
        TITLE,
        IMAGE_TYPE_1_ICON,
        IMAGE_TYPE_3_MAIN,
        DATA_TYPE_1_SPONSORED,
        DATA_TYPE_2_DESCRIPTION,
        DATA_TYPE_3_RATING,
        DATA_TYPE_4_LIKES,
        DATA_TYPE_5_DOWNLOADS,
        DATA_TYPE_6_PRICE,
        DATA_TYPE_7_SALE_PRICE,
        DATA_TYPE_8_PHONE,
        DATA_TYPE_9_ADDRESS,
        DATA_TYPE_10_ADDITIONAL_DESCRIPTION,
        DATA_TYPE_11_DISPLAY_URL,
        DATA_TYPE_12_CTA_TEXT,
    }
    public NativeAdUnit(@NonNull String configId) {
        super(configId, AdType.NATIVE);
    }

    NATIVE_REQUEST_VERSION request_version = NATIVE_REQUEST_VERSION.VERSION_1_1;
    HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = new HashMap<>();

    public void setNativeRequestAPIVersion(NATIVE_REQUEST_VERSION version){
        this.request_version = version;
    }

    public void addTitle(Integer len, Boolean required){
        HashMap<String, Object> params = new HashMap<>();
        params.put("len", len);
        params.put("required", required);
        assets.put(NATIVE_REQUEST_ASSET.TITLE, params);
    }

    public void addImage(Integer type, Integer wmin, Integer hmin, Integer w, Integer h, ArrayList<String> mimes, Boolean required) throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        params.put("wmin", wmin);
        params.put("hmin", hmin);
        params.put("h", h);
        params.put("w", w);
        params.put("required", required);
        if (mimes != null) {
            String mimesString = "";
            for (String mime : mimes) {
                mimesString += mime;
                mimesString += ",";
            }
            params.put("mimes", mimesString);
        }

        switch (type) {
            case 1:
                assets.put(NATIVE_REQUEST_ASSET.IMAGE_TYPE_1_ICON, params);
                break;
            case 3:
                assets.put(NATIVE_REQUEST_ASSET.IMAGE_TYPE_3_MAIN, params);
                break;
            default:
                throw new Exception("Unsupported type " + type);
        }
    }

    public void addData(Integer type, Integer len, Boolean required) throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        params.put("len", len);
        params.put("required", required);
        switch (type) {
            case 1:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_1_SPONSORED, params);
                break;
            case 2:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_2_DESCRIPTION, params);
                break;
            case 3:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_3_RATING, params);
                break;
            case 4:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_4_LIKES, params);
                break;
            case 5:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_5_DOWNLOADS, params);
                break;
            case 6:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_6_PRICE, params);
                break;
            case 7:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_7_SALE_PRICE, params);
                break;
            case 8:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_8_PHONE, params);
                break;
            case 9:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_9_ADDRESS, params);
                break;
            case 10:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_10_ADDITIONAL_DESCRIPTION, params);
                break;
            case 11:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_11_DISPLAY_URL, params);
                break;
            case 12:
                assets.put(NATIVE_REQUEST_ASSET.DATA_TYPE_12_CTA_TEXT, params);
                break;
            default:
                throw new Exception("Unsupported type " + type);
        }
    }
}
