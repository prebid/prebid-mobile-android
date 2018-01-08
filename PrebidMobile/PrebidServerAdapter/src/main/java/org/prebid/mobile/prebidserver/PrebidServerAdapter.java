package org.prebid.mobile.prebidserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.core.AdSize;
import org.prebid.mobile.core.AdUnit;
import org.prebid.mobile.core.BidManager;
import org.prebid.mobile.core.BidResponse;
import org.prebid.mobile.core.CacheManager;
import org.prebid.mobile.core.DemandAdapter;
import org.prebid.mobile.core.ErrorCode;
import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.core.TargetingParams;
import org.prebid.mobile.prebidserver.internal.AdvertisingIDUtil;
import org.prebid.mobile.prebidserver.internal.Settings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

public class PrebidServerAdapter implements DemandAdapter, ServerConnector.ServerListener {

    //region instance members
    private ArrayList<AdUnit> adUnits; // list of current ad units in the flight
    private WeakReference<BidManager.BidResponseListener> weakReferenceLisenter;

    @Override
    public void requestBid(final Context context, final BidManager.BidResponseListener bidResponseListener, final ArrayList<AdUnit> adUnits) {
        this.adUnits = adUnits;
        this.weakReferenceLisenter = new WeakReference<BidManager.BidResponseListener>(bidResponseListener);
        JSONObject postData = getPostData(context, adUnits);
        if (Prebid.isSecureConnection()) {
            new ServerConnector(postData, this, Settings.REQUEST_URL_SECURE, context).execute();
        } else {
            new ServerConnector(postData, this, Settings.REQUEST_URL_NON_SECURE, context).execute();
        }
    }

    @Override
    public void onServerResponded(JSONObject response) {
        BidManager.BidResponseListener bidResponseListener = this.weakReferenceLisenter.get();
        if (bidResponseListener != null) {
            HashMap<AdUnit, ArrayList<BidResponse>> responses = new HashMap<AdUnit, ArrayList<BidResponse>>();
            if (response == null || response.length() == 0) {
                LogUtil.e("empty server response.");
            } else {
                // check response status first
                String status;
                try {
                    status = response.getString(Settings.RESPONSE_STATUS);
                } catch (JSONException e) {
                    LogUtil.e("Unable to retrieve response status.");
                    return;
                }
                if (status == null || !status.equals(Settings.RESPONSE_STATUS_OK)) {
                    LogUtil.e("Response status is not OK.");
                    return;
                }
                // check response tid
                String tid;
                try {
                    tid = response.getString(Settings.RESPONSE_TID);
                } catch (JSONException e) {
                    LogUtil.e("Unable to retrieve tid from response.");
                    return;
                }
                if (tid == null || !tid.equals(currentTID)) {
                    // todo, think about when the request method is called multiple times?
                    LogUtil.e("tid in response does not match the one in request.");
                    return;
                }
                JSONArray bids = null;
                try {
                    bids = response.getJSONArray(Settings.RESPONSE_BIDS);
                } catch (JSONException e) {
                    LogUtil.e("Server response does not contain bids array");
                }
                if (bids != null && bids.length() != 0) {
                    int length = bids.length();
                    for (int i = 0; i < length; i++) {
                        try {
                            JSONObject bid = bids.getJSONObject(i);
                            String code = bid.getString(Settings.RESPONSE_CODE);
                            AdUnit adUnit = getAdUnitByCode(code);
                            if (adUnit != null) {
                                final double bidPrice = bid.getDouble(Settings.RESPONSE_PRICE);
                                String bidder = bid.getString(Settings.RESPONSE_BIDDER);
                                ArrayList<BidResponse> responseList = responses.get(adUnit);
                                if (responseList == null) {
                                    responseList = new ArrayList<BidResponse>();
                                }
                                BidResponse newBid;
                                if (Prebid.getAdServer() == Prebid.AdServer.DFP) {
                                    JSONObject targetingKeywords = bid.getJSONObject(Settings.RESPONSE_TARGETING);
                                    String format = targetingKeywords.getString(Settings.RESPONSE_CREATIVE);
                                    String cacheId = CacheManager.getCacheManager().saveCache(bid.toString(), format);
                                    newBid = new BidResponse(bidPrice, cacheId);
                                    newBid.setBidderCode(bidder);
                                    Iterator<?> keys = targetingKeywords.keys();
                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        if (key.startsWith("hb_cache_id")) {
                                            newBid.addCustomKeyword(key, cacheId);
                                        } else {
                                            newBid.addCustomKeyword(key, targetingKeywords.getString(key));
                                        }
                                    }
                                } else {
                                    JSONObject targetingKeywords = bid.getJSONObject(Settings.RESPONSE_TARGETING);
                                    String cacheId = targetingKeywords.getString(Settings.RESPONSE_CACHE_ID);
                                    newBid = new BidResponse(bidPrice, cacheId);
                                    Iterator<?> keys = targetingKeywords.keys();
                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        newBid.addCustomKeyword(key, targetingKeywords.getString(key));
                                    }
                                }
                                responseList.add(newBid);
                                responses.put(adUnit, responseList);
                            }
                        } catch (JSONException e) {
                            LogUtil.e(String.format(Locale.ENGLISH, "Error parsing bids array: %s", bids.toString()));
                        }
                    }
                }
            }
            for (AdUnit adUnit : adUnits) {
                ArrayList<BidResponse> results = responses.get(adUnit);
                if (results != null && !results.isEmpty()) {
                    bidResponseListener.onBidSuccess(adUnit, results);
                } else {
                    bidResponseListener.onBidFailure(adUnit, ErrorCode.NO_BIDS);
                }
            }
        }
    }

    private AdUnit getAdUnitByCode(String code) {
        if (adUnits != null && !adUnits.isEmpty()) {
            for (AdUnit adUnit : adUnits) {
                if (adUnit.getCode().equals(code)) {
                    return adUnit;
                }
            }
        }
        return null;
    }

    JSONObject getPostData(Context context, ArrayList<AdUnit> adUnits) {
        if (context != null) {
            AdvertisingIDUtil.retrieveAndSetAAID(context);
            Settings.update(context);
        }
        JSONObject postData = new JSONObject();
        try {
            postData.put(Settings.REQUEST_SORT_BIDS, 1);
            postData.put(Settings.REQUEST_TID, generateTID());
            postData.put(Settings.REQUEST_ACCOUNT_ID, Prebid.getAccountId());
            postData.put(Settings.REQUEST_MAX_KEY, 20);
            if (Prebid.getAdServer() == Prebid.AdServer.DFP) {
                postData.put(Settings.REQUEST_CACHE_MARKUP, 0);
            } else {
                postData.put(Settings.REQUEST_CACHE_MARKUP, 1);
            }
            // add ad units
            JSONArray adUnitConfigs = getAdUnitConfigs(adUnits);
            if (adUnitConfigs != null && adUnitConfigs.length() > 0) {
                postData.put(Settings.REQUEST_AD_UNITS, adUnitConfigs);
            }
            // add device
            JSONObject device = getDeviceObject(context);
            if (device != null && device.length() > 0) {
                postData.put(Settings.REQUEST_DEVICE, device);
            }
            // add app
            JSONObject app = getAppObject(context);
            if (device != null && device.length() > 0) {
                postData.put(Settings.REQUEST_APP, app);
            }
            // todo the following are not in pbs_request.json, add request to support this?
            // add user
            // todo should we provide api for developers to pass in user's location (zip, city, address etc, not real time location)
            JSONObject user = getUserObject();
            if (user != null && user.length() > 0) {
                postData.put(Settings.REQUEST_USER, user);
            }
            // add custom keywords
            JSONArray keywords = getCustomKeywordsArray();
            if (keywords != null && keywords.length() > 0) {
                postData.put(Settings.REQUEST_KEYWORDS, keywords);
            }
            JSONObject version = getSDKVersion();
            if (version != null && version.length() > 0) {
                postData.put(Settings.REQUEST_SDK, version);
            }
        } catch (JSONException e) {
        }
        return postData;
    }

    private String currentTID = null;

    private String generateTID() {
        currentTID = UUID.randomUUID().toString();
        return currentTID;
    }

    private JSONObject getSDKVersion() {
        JSONObject version = new JSONObject();
        try {
            version.put(Settings.REQUEST_SDK_SOURCE, Settings.REQUEST_SDK_MOBILE);
            version.put(Settings.REQUEST_SDK_VERSION, Settings.sdk_version);
            version.put(Settings.REQUEST_SDK_PLATFORM, Settings.REQUEST_SDK_ANDROID);
        } catch (JSONException e) {
        }
        return version;
    }

    private JSONArray getAdUnitConfigs(ArrayList<AdUnit> adUnits) {
        JSONArray adUnitConfigs = new JSONArray();
        for (AdUnit adUnit : adUnits) {
            // takes information from the ad units
            // look up the configuration of the ad unit
            try {
                JSONObject adUnitConfig = new JSONObject();
                adUnitConfig.put(Settings.REQUEST_CONFIG_ID, adUnit.getConfigId());
                adUnitConfig.put(Settings.REQUEST_CODE, adUnit.getCode());
                JSONArray sizes = new JSONArray();
                for (AdSize size : adUnit.getSizes()) {
                    JSONObject sizeConfig = new JSONObject();
                    sizeConfig.put(Settings.REQUEST_WIDTH, size.getWidth());
                    sizeConfig.put(Settings.REQUEST_HEIGHT, size.getHeight());
                    sizes.put(sizeConfig);
                }
                adUnitConfig.put(Settings.REQUEST_SIZES, sizes);
                adUnitConfigs.put(adUnitConfig);
            } catch (JSONException e) {
            }
        }
        return adUnitConfigs;
    }

    private JSONObject getDeviceObject(Context context) {
        JSONObject device = new JSONObject();
        try {
            // Device make
            if (!TextUtils.isEmpty(Settings.deviceMake))
                device.put(Settings.REQUEST_DEVICE_MAKE, Settings.deviceMake);
            // Device model
            if (!TextUtils.isEmpty(Settings.deviceModel))
                device.put(Settings.REQUEST_DEVICE_MODEL, Settings.deviceModel);
            // Default User Agent
            if (!TextUtils.isEmpty(Settings.userAgent)) {
                device.put(Settings.REQUEST_USERAGENT, Settings.userAgent);
            }
            // POST data that requires context
            if (context != null) {
                device.put(Settings.REQUEST_DEVICE_WIDTH, context.getResources().getConfiguration().screenWidthDp);
                device.put(Settings.REQUEST_DEVICE_HEIGHT, context.getResources().getConfiguration().screenHeightDp);

                device.put(Settings.REQUEST_DEVICE_PIXEL_RATIO, context.getResources().getDisplayMetrics().density);

                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                // Get mobile country codes
                if (Settings.getMCC() < 0 || Settings.getMNC() < 0) {
                    String networkOperator = telephonyManager.getNetworkOperator();
                    if (!TextUtils.isEmpty(networkOperator)) {
                        try {
                            Settings.setMCC(Integer.parseInt(networkOperator.substring(0, 3)));
                            Settings.setMNC(Integer.parseInt(networkOperator.substring(3)));
                        } catch (Exception e) {
                            // Catches NumberFormatException and StringIndexOutOfBoundsException
                            Settings.setMCC(-1);
                            Settings.setMNC(-1);
                        }
                    }
                }
                if (Settings.getMCC() > 0 && Settings.getMNC() > 0) {
                    device.put(Settings.REQUEST_MCC_MNC, String.format(Locale.ENGLISH, "%d-%d", Settings.getMCC(), Settings.getMNC()));
                }

                // Get carrier
                if (Settings.getCarrierName() == null) {
                    try {
                        Settings.setCarrierName(telephonyManager.getNetworkOperatorName());
                    } catch (SecurityException ex) {
                        // Some phones require READ_PHONE_STATE permission just ignore name
                        Settings.setCarrierName("");
                    }
                }
                if (!TextUtils.isEmpty(Settings.getCarrierName()))
                    device.put(Settings.REQUEST_CARRIER, Settings.getCarrierName());

                // check connection type
                int connection_type = 0;
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifi != null) {
                        connection_type = wifi.isConnected() ? 1 : 2;
                    }
                }
                device.put(Settings.REQUEST_CONNECTION_TYPE, connection_type);
            }
            // Location Settings
            Double lat, lon;
            Integer locDataAge, locDataPrecision;
            Location lastLocation = null;
            Location appLocation = TargetingParams.getLocation();
            // Do we have access to location?
            if (TargetingParams.getLocationEnabled()) {
                // First priority is the app developer supplied location
                if (appLocation != null) {
                    lastLocation = appLocation;
                }
                // If app developer didn't provide any, get lat, long from any GPS information
                // that might be currently available through Android LocationManager
                else if (context != null
                        && (context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
                        || context.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED)) {

                    LocationManager lm = (LocationManager) context
                            .getSystemService(Context.LOCATION_SERVICE);

                    for (String provider_name : lm.getProviders(true)) {
                        Location l = lm.getLastKnownLocation(provider_name);
                        if (l == null) {
                            continue;
                        }

                        if (lastLocation == null) {
                            lastLocation = l;
                        } else {
                            if (l.getTime() > 0 && lastLocation.getTime() > 0) {
                                if (l.getTime() > lastLocation.getTime()) {
                                    lastLocation = l;
                                }
                            }
                        }
                    }
                } else {
                    LogUtil.w(Settings.TAG,
                            "Location permissions ACCESS_COARSE_LOCATION and/or ACCESS_FINE_LOCATION aren\\'t set in the host app. This may affect demand.");
                }
            }

            // Set the location info back to the application
            // If location was not enabled, null value will override the location data in the ANTargeting
            if (appLocation != lastLocation) {
                TargetingParams.setLocation(lastLocation);
            }

            if (lastLocation != null) {
                if (TargetingParams.getLocationDecimalDigits() <= -1) {
                    lat = lastLocation.getLatitude();
                    lon = lastLocation.getLongitude();
                } else {
                    lat = Double.parseDouble(String.format(Locale.ENGLISH, "%." + TargetingParams.getLocationDecimalDigits() + "f", lastLocation.getLatitude()));
                    lon = Double.parseDouble(String.format(Locale.ENGLISH, "%." + TargetingParams.getLocationDecimalDigits() + "f", lastLocation.getLongitude()));
                }
                locDataPrecision = Math.round(lastLocation.getAccuracy());
                //Don't report location data from the future
                locDataAge = (int) Math.max(0, (System.currentTimeMillis() - lastLocation.getTime()));
            } else {
                lat = null;
                lon = null;
                locDataAge = null;
                locDataPrecision = null;
            }
            JSONObject geo = new JSONObject();
            if (lat != null && lon != null) {
                geo.put(Settings.REQEUST_GEO_LAT, lat);
                geo.put(Settings.REQUEST_GEO_LON, lon);
                if (locDataAge != null) geo.put(Settings.REQUEST_GEO_AGE, locDataAge);
                if (locDataPrecision != null)
                    geo.put(Settings.REQUEST_GEO_ACCURACY, locDataPrecision);
            }
            if (geo.length() > 0) {
                device.put(Settings.REQUEST_GEO, geo);
            }

            // devtime
            long dev_time = System.currentTimeMillis();
            device.put(Settings.REQUEST_DEVTIME, dev_time);

            // limited ad tracking
            device.put(Settings.REQUEST_LMT, AdvertisingIDUtil.isLimitAdTracking() ? 1 : 0);
            if (!AdvertisingIDUtil.isLimitAdTracking() && !TextUtils.isEmpty(AdvertisingIDUtil.getAAID())) {
                // put ifa
                device.put(Settings.REQUEST_IFA, AdvertisingIDUtil.getAAID());
            }

            // os
            device.put(Settings.REQUEST_OS, Settings.os);
            device.put(Settings.REQUEST_OS_VERSION, String.valueOf(Build.VERSION.SDK_INT));
        } catch (JSONException e) {
        }
        return device;
    }

    private JSONObject getAppObject(Context context) {
        if (TextUtils.isEmpty(Settings.getAppID())) {
            if (context != null) {
                Settings.setAppID(context.getApplicationContext()
                        .getPackageName());
            }
        }
        JSONObject app = new JSONObject();
        try {
            app.put(Settings.REQUEST_APP_BUNDLE, Settings.getAppID());
            app.put(Settings.REQUEST_APP_VERSION, Settings.pkgVersion);
            app.put(Settings.REQUEST_APP_NAME, Settings.appName);
            app.put(Settings.REQUEST_APP_DOMAIN, Settings.getDomain());
            app.put(Settings.REQUEST_APP_STOREURL, Settings.getStoreUrl());
            app.put(Settings.REQUEST_APP_PRIVACY, Settings.getPrivacyPolicy());
            // todo get paid, keywords
        } catch (JSONException e) {
        }
        return app;

    }

    private JSONObject getUserObject() {
        JSONObject user = new JSONObject();
        try {
            if (TargetingParams.getAge() > 0) {
                user.put(Settings.REQUEST_AGE, TargetingParams.getAge());
            }
            TargetingParams.GENDER gender = TargetingParams.getGender();
            String g = "O";
            switch (gender) {
                case FEMALE:
                    g = "F";
                    break;
                case MALE:
                    g = "M";
                    break;
                case UNKNOWN:
                    g = "O";
                    break;
            }
            user.put(Settings.REQUEST_GENDER, g);
            if (!TextUtils.isEmpty(Settings.language)) {
                user.put(Settings.REQUEST_LANGUAGE, Settings.language);
            }
        } catch (JSONException e) {
        }
        return user;
    }

    // TODO not supported, add request to support this?
    private JSONArray getCustomKeywordsArray() {
        JSONArray keywords = new JSONArray();
        // add custom parameters if there are any
        HashMap<String, ArrayList<String>> customKeywords = TargetingParams.getCustomKeywords();
        if (customKeywords != null && !customKeywords.isEmpty()) {
            Iterator it = customKeywords.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                ArrayList<String> values = customKeywords.get(key);
                if (!TextUtils.isEmpty(key) && values != null && !values.isEmpty()) {
                    try {
                        JSONObject key_val = new JSONObject();
                        key_val.put(Settings.REQUEST_KEY, key);
                        JSONArray val = new JSONArray(values);
                        key_val.put(Settings.REQUEST_VALUE, val);
                        keywords.put(key_val);
                    } catch (JSONException e) {
                    }
                }
            }
        }
        return keywords;
    }
    //endregion
}
