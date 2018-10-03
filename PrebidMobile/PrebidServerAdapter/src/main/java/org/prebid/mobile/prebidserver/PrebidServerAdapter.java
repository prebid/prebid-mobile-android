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
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.core.AdSize;
import org.prebid.mobile.core.AdType;
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
    private final int ADUNITS_PER_REQUEST = 10;

    @Override
    public void requestBid(final Context context, final BidManager.BidResponseListener bidResponseListener, final ArrayList<AdUnit> adUnits) {
        this.adUnits = adUnits;
        this.weakReferenceLisenter = new WeakReference<BidManager.BidResponseListener>(bidResponseListener);
        // Batch 10 calls for each request to server

        ArrayList<ArrayList<AdUnit>> adUnitsList = batchAdUnits(adUnits);
        if (adUnitsList != null && !adUnitsList.isEmpty()) {
            for (ArrayList batchedAdUnits : adUnitsList) {
                JSONObject postData = getPostData(context, batchedAdUnits);
                LogUtil.d(Settings.TAG, "Prebid Mobile send request with: " + postData.toString());
                new ServerConnector(postData, this, getHost(), context).execute();
            }
        }
    }

    ArrayList<ArrayList<AdUnit>> batchAdUnits(ArrayList<AdUnit> adUnits) {
        ArrayList<ArrayList<AdUnit>> adUnitsList = new ArrayList<>();
        int len = adUnits.size();
        int i = len / ADUNITS_PER_REQUEST;
        int j = len % ADUNITS_PER_REQUEST;

        for (int m = 0; m < i; m++) {
            ArrayList<AdUnit> splitAdUnitsList = new ArrayList<>();
            for (int n = 0; n < ADUNITS_PER_REQUEST; n++) {
                splitAdUnitsList.add(adUnits.get(m * 10 + n));
            }
            adUnitsList.add(splitAdUnitsList);
        }
        if (j > 0) {
            ArrayList<AdUnit> splitAdUnitsList = new ArrayList<>();
            for (int n = 0; n < j; n++) {
                splitAdUnitsList.add(adUnits.get(i * ADUNITS_PER_REQUEST + n));
            }
            adUnitsList.add(splitAdUnitsList);
        }
        return adUnitsList;
    }

    String getHost() {
        String host = null;
        switch (Prebid.getHost()) {
            case APPNEXUS:
                host = (Prebid.isSecureConnection()) ? Settings.APPNEXUS_REQUEST_URL_SECURE :
                        Settings.APPNEXUS_REQUEST_URL_NON_SECURE;
                break;
            case RUBICON:
                host = (Prebid.isSecureConnection()) ? Settings.RUBICON_REQUEST_URL_SECURE :
                        Settings.RUBICON_REQUEST_URL_NON_SECURE;
                break;
        }
        return host;
    }

    @Override
    public void onServerResponded(JSONObject response) {
        BidManager.BidResponseListener bidResponseListener = this.weakReferenceLisenter.get();
        if (bidResponseListener != null) {
            HashMap<AdUnit, ArrayList<BidResponse>> responses = new HashMap<AdUnit, ArrayList<BidResponse>>();
            if (response == null || response.length() == 0) {
                LogUtil.e(Settings.TAG, "Server responded with empty response.");
            } else {
                LogUtil.d(Settings.TAG, "Server responded with: " + response.toString());
                // check if seatbid is presented in the response first
                JSONArray seatbid = null;
                try {
                    seatbid = response.getJSONArray("seatbid");
                } catch (JSONException e) {
                }
                if (seatbid != null && seatbid.length() > 0) {
                    int len = seatbid.length();
                    for (int i = 0; i < len; i++) {
                        try {
                            JSONObject seat = seatbid.getJSONObject(i);
                            String bidderName = seat.getString("seat");
                            JSONArray bids = seat.getJSONArray("bid");
                            if (bids != null && bids.length() > 0) {
                                int bidLen = bids.length();
                                for (int j = 0; j < bidLen; j++) {
                                    JSONObject bid = bids.getJSONObject(j);
                                    String impId = bid.getString("impid");
                                    AdUnit adUnit = getAdUnitByCode(impId);
                                    if (adUnit != null) {
                                        final double bidPrice = bid.getDouble("price");
                                        ArrayList<BidResponse> responseList = responses.get(adUnit);
                                        if (responseList == null) {
                                            responseList = new ArrayList<BidResponse>();
                                        }
                                        JSONObject targetingKeywords = null;
                                        try {
                                            targetingKeywords = bid.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting");
                                        } catch (JSONException e) {

                                        }
                                        if (targetingKeywords != null && targetingKeywords.length() > 0) {
                                            BidResponse newBid = null;
                                            if (Prebid.useLocalCache()) {
                                                String format = targetingKeywords.getString("hb_creative_loadtype");
                                                String cacheId = CacheManager.getCacheManager().saveCache(bid.toString(), format);
                                                newBid = new BidResponse(bidPrice, cacheId);
                                                newBid.setBidderCode(bidderName);
                                                Iterator<?> keys = targetingKeywords.keys();
                                                while (keys.hasNext()) {
                                                    String key = (String) keys.next();
                                                    newBid.addCustomKeyword(key, targetingKeywords.getString(key));
                                                }
                                                String cacheIdKey = "hb_cache_id_" + bidderName;
                                                cacheIdKey = cacheIdKey.substring(0, Math.min(cacheIdKey.length(), Settings.REQUEST_KEY_LENGTH_MAX));
                                                newBid.addCustomKeyword(cacheIdKey, cacheId);
                                            } else {
                                                String cacheId = null;
                                                Iterator keyIterator = targetingKeywords.keys();
                                                while (keyIterator.hasNext()) {
                                                    String key = (String) keyIterator.next();
                                                    if (key.startsWith("hb_cache_id_")) {
                                                        cacheId = targetingKeywords.getString(key);
                                                    }
                                                }
                                                if (cacheId != null) {
                                                    newBid = new BidResponse(bidPrice, cacheId);
                                                    newBid.setBidderCode(bidderName);
                                                    Iterator<?> keys = targetingKeywords.keys();
                                                    while (keys.hasNext()) {
                                                        String key = (String) keys.next();
                                                        newBid.addCustomKeyword(key, targetingKeywords.getString(key));
                                                    }
                                                }
                                            }
                                            if (newBid != null) {
                                                responseList.add(newBid);
                                            }
                                        }
                                        responses.put(adUnit, responseList);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
            }
            for (AdUnit adUnit : adUnits) {
                ArrayList<BidResponse> results = responses.get(adUnit);
                if (results != null && !results.isEmpty()) {
                    // save the bids sorted
                    if (Prebid.useLocalCache()) {
                        BidResponse topBid = null;
                        for (int i = 0; i < results.size(); i++) {
                            for (Pair<String, String> pair : results.get(i).getCustomKeywords()) {
                                if (pair.first.equals("hb_bidder")) {
                                    topBid = results.get(i);
                                }
                            }
                        }
                        if (topBid != null) {
                            topBid.addCustomKeyword("hb_cache_id", topBid.getCreative());
                        }
                        bidResponseListener.onBidSuccess(adUnit, results);
                    } else {
                        // in the case that `hb_cache_id` is not present in any bids, do not pass the response back to publisher
                        String topCacheId = null;
                        for (BidResponse bid : results) {
                            ArrayList<Pair<String, String>> keywords = bid.getCustomKeywords();
                            for (Pair<String, String> pair : keywords) {
                                if (pair.first.equals("hb_cache_id")) topCacheId = pair.second;
                            }
                        }
                        if (topCacheId != null) {
                            bidResponseListener.onBidSuccess(adUnit, results);
                        } else {
                            bidResponseListener.onBidFailure(adUnit, ErrorCode.NO_BIDS);
                        }
                    }
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
            String id = UUID.randomUUID().toString();
            postData.put("id", id);
            JSONObject source = new JSONObject();
            source.put("tid", id);
            postData.put("source", source);
            // add ad units
            JSONArray imps = getImps(adUnits);
            if (imps != null && imps.length() > 0) {
                postData.put("imp", imps);
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
            // add user
            // todo should we provide api for developers to pass in user's location (zip, city, address etc, not real time location)
            JSONObject user = getUserObject(context);
            if (user != null && user.length() > 0) {
                postData.put(Settings.REQUEST_USER, user);
            }
            // add regs
            JSONObject regs = getRegsObject(context);
            if (regs != null && regs.length() > 0) {
                postData.put("regs", regs);
            }
            // add targeting keywords request
            JSONObject ext = getRequestExtData();
            if (ext != null && ext.length() > 0) {
                postData.put("ext", ext);
            }
        } catch (JSONException e) {
        }
        return postData;
    }

    private JSONObject getRequestExtData() {
        JSONObject ext = new JSONObject();
        JSONObject prebid = new JSONObject();
        try {
            if (!Prebid.useLocalCache()) {
                JSONObject bids = new JSONObject();
                JSONObject cache = new JSONObject();
                cache.put("bids", bids);
                prebid.put("cache", cache);
            }
            JSONObject storedRequest = new JSONObject();
            storedRequest.put("id", Prebid.getAccountId());
            prebid.put("storedrequest", storedRequest);
            JSONObject targetingEmpty = new JSONObject();
            prebid.put("targeting", targetingEmpty); 
            ext.put("prebid", prebid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ext;
    }


    private JSONArray getImps(ArrayList<AdUnit> adUnits) {
        JSONArray impConfigs = new JSONArray();
        for (AdUnit adUnit : adUnits) {
            // takes information from the ad units
            // look up the configuration of the ad unit
            try {
                JSONObject imp = new JSONObject();
                JSONObject ext = new JSONObject();
                imp.put("id", adUnit.getCode());
                if (Prebid.isSecureConnection()) {
                    imp.put("secure", 1);
                }
                if (adUnit.getAdType().equals(AdType.INTERSTITIAL)) {
                    imp.put("instl", 1);
                }
                imp.put("ext", ext);
                JSONObject prebid = new JSONObject();
                ext.put("prebid", prebid);
                JSONObject storedrequest = new JSONObject();
                prebid.put("storedrequest", storedrequest);
                storedrequest.put("id", adUnit.getConfigId());
                imp.put("ext", ext);
                JSONObject banner = new JSONObject();
                JSONArray format = new JSONArray();
                for (AdSize size : adUnit.getSizes()) {
                    format.put(new JSONObject().put("w", size.getWidth()).put("h", size.getHeight()));
                }
                banner.put("format", format);
                imp.put("banner", banner);
                impConfigs.put(imp);
            } catch (JSONException e) {
            }
        }
        return impConfigs;
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

            // limited ad tracking
            device.put(Settings.REQUEST_LMT, AdvertisingIDUtil.isLimitAdTracking() ? 1 : 0);
            if (!AdvertisingIDUtil.isLimitAdTracking() && !TextUtils.isEmpty(AdvertisingIDUtil.getAAID())) {
                // put ifa
                device.put(Settings.REQUEST_IFA, AdvertisingIDUtil.getAAID());
            }

            // os
            device.put(Settings.REQUEST_OS, Settings.os);
            device.put(Settings.REQUEST_OS_VERSION, String.valueOf(Build.VERSION.SDK_INT));
            // language
            if (!TextUtils.isEmpty(Settings.language)) {
                device.put(Settings.REQUEST_LANGUAGE, Settings.language);
            }
        } catch (JSONException e) {
        }
        return device;
    }

    private JSONObject getAppObject(Context context) {
        if (TextUtils.isEmpty(TargetingParams.getBundleName())) {
            if (context != null) {
                TargetingParams.setBundleName(context.getApplicationContext()
                        .getPackageName());
            }
        }
        JSONObject app = new JSONObject();
        try {
            if (!TextUtils.isEmpty(TargetingParams.getBundleName())) {
                app.put("bundle", TargetingParams.getBundleName());
            }
            if (!TextUtils.isEmpty(Settings.pkgVersion)) {
                app.put("ver", Settings.pkgVersion);
            }
            if (!TextUtils.isEmpty(Settings.appName)) {
                app.put("name", Settings.appName);
            }
            if (!TextUtils.isEmpty(TargetingParams.getDomain())) {
                app.put("domain", TargetingParams.getDomain());
            }
            if (!TextUtils.isEmpty(TargetingParams.getStoreUrl())) {
                app.put("storeurl", TargetingParams.getStoreUrl());
            }
            app.put("privacypolicy", TargetingParams.getPrivacyPolicy());
            JSONObject publisher = new JSONObject();
            publisher.put("id", Prebid.getAccountId());
            app.put("publisher", publisher);
            JSONObject prebid = new JSONObject();
            prebid.put("source", "prebid-mobile");
            prebid.put("version", Settings.sdk_version);
            JSONObject ext = new JSONObject();
            ext.put("prebid", prebid);
            app.put("ext", ext);
        } catch (JSONException e) {
        }
        return app;

    }

    private JSONObject getUserObject(Context context) {
        JSONObject user = new JSONObject();
        try {
            if (TargetingParams.getYearOfBirth() > 0) {
                user.put("yob", TargetingParams.getYearOfBirth());
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
            user.put("gender", g);
            StringBuilder builder = new StringBuilder();
            ArrayList<String> keywords = TargetingParams.getUserKeywords();
            for (String key : keywords) {
                builder.append(key).append(",");
            }
            String finalKeywords = builder.toString();
            if (!TextUtils.isEmpty(finalKeywords)) {
                user.put("keywords", finalKeywords);
            }
            if (TargetingParams.isSubjectToGDPR(context) != null) {
                JSONObject ext = new JSONObject();
                ext.put("consent", TargetingParams.getGDPRConsentString(context));
                user.put("ext", ext);
            }
        } catch (JSONException e) {
        }
        return user;
    }

    private JSONObject getRegsObject(Context context) {
        JSONObject regs = new JSONObject();
        try {
            JSONObject ext = new JSONObject();
            if (TargetingParams.isSubjectToGDPR(context) != null) {
                if (TargetingParams.isSubjectToGDPR(context)) {
                    ext.put("gdpr", 1);
                } else {
                    ext.put("gdpr", 0);
                }
            }
            regs.put("ext", ext);
        } catch (JSONException e) {
        }
        return regs;
    }
    //endregion
}
