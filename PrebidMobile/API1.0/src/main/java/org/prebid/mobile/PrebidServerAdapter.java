package org.prebid.mobile;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PrebidServerAdapter implements DemandAdapter {
    private ArrayList<ServerConnector> serverConnectors;

    public PrebidServerAdapter() {
        serverConnectors = new ArrayList<>();
    }

    @Override
    public void requestDemand(RequestParams params, DemandAdapterListener listener, String auctionId) {
        ServerConnector connector = new ServerConnector(listener, params, auctionId);
        serverConnectors.add(connector);
        connector.execute();
    }

    @Override
    public void stopRequest(String auctionId) {
        ArrayList<ServerConnector> toRemove = new ArrayList<>();
        for (ServerConnector connector : serverConnectors) {
            if (connector.getAuctionId().equals(auctionId)) {
                connector.destroy();
                toRemove.add(connector);
            }
        }
        serverConnectors.removeAll(toRemove);
    }

    static class ServerConnector extends AsyncTask<Object, Object, JSONObject> {
        private DemandAdapterListener listener;
        private RequestParams requestParams;
        private String auctionId;

        ServerConnector(DemandAdapterListener listener, RequestParams requestParams, String auctionId) {
            this.listener = listener;
            this.requestParams = requestParams;
            this.auctionId = auctionId;
        }

        @Override
        protected JSONObject doInBackground(Object... objects) {
            try {
                URL url = new URL(getHost());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                String existingCookie = getExistingCookie();
                if (existingCookie != null) {
                    conn.setRequestProperty(PrebidServerSettings.COOKIE_HEADER, existingCookie);
                } // todo still pass cookie if limit ad tracking?

                conn.setRequestMethod("POST");
                conn.setConnectTimeout(DemandFetcher.timeoutMillis);

                // Add post data
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                JSONObject postData = getPostData(this.requestParams);
                LogUtil.d("Sending request for auction " + auctionId + " with post data: " + postData.toString());
                wr.write(postData.toString());
                wr.flush();

                // Start the connection
                conn.connect();

                // Read request response
                int httpResult = conn.getResponseCode();

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();
                    is.close();
                    String result = builder.toString();
                    JSONObject response = new JSONObject(result);
                    httpCookieSync(conn.getHeaderFields());
                    // in the future, this can be improved to parse response base on request versions
                    return response;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace(); // catches SocketTimeOutException, etc
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            HashMap<String, String> keywords = new HashMap<>();
            if (jsonObject != null) {
                try {
                    JSONArray seatbid = jsonObject.getJSONArray("seatbid");
                    if (seatbid != null) {
                        for (int i = 0; i < seatbid.length(); i++) {
                            JSONObject seat = seatbid.getJSONObject(i);
                            JSONArray bids = seat.getJSONArray("bid");
                            if (bids != null) {
                                for (int j = 0; j < bids.length(); j++) {
                                    JSONObject bid = bids.getJSONObject(i);
                                    JSONObject hb_key_values = bid.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting");
                                    Iterator it = hb_key_values.keys();
                                    while (it.hasNext()) {
                                        String key = (String) it.next();
                                        keywords.put(key, hb_key_values.getString(key));
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (listener != null) {
                if (!keywords.isEmpty() && keywords.keySet().contains("hb_cache_id")) {
                    listener.onDemandReady(keywords, auctionId);
                } else {
                    listener.onDemandFailed(ResultCode.NO_BIDS, auctionId);
                }

            }
            serverConnectors.remove(this);
        }

        String getAuctionId() {
            return auctionId;
        }

        void destroy() {
            this.cancel(true);
            this.listener = null;
        }

        private String getHost() {
            return PrebidMobile.getHost().getHostUrl();
        }

        /**
         * Synchronize the uuid2 cookie to the Webview Cookie Jar
         * This is only done if there is no present cookie.
         *
         * @param headers headers to extract cookies from for syncing
         */
        @SuppressWarnings("deprecation")
        private void httpCookieSync(Map<String, List<String>> headers) {
            if (headers == null || headers.isEmpty()) return;
            CookieManager cm = CookieManager.getInstance();
            if (cm == null) {
                LogUtil.i("PrebidNewAPI", "Unable to find a CookieManager");
                return;
            }
            try {
                String existingUUID = getExistingCookie();

                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    // Only "Set-cookie" and "Set-cookie2" pair will be parsed
                    if (key != null && (key.equalsIgnoreCase(PrebidServerSettings.VERSION_ZERO_HEADER)
                            || key.equalsIgnoreCase(PrebidServerSettings.VERSION_ONE_HEADER))) {
                        for (String cookieStr : entry.getValue()) {
                            if (!TextUtils.isEmpty(cookieStr) && cookieStr.contains(PrebidServerSettings.AN_UUID)) {
                                // pass uuid2 to WebView Cookie jar if it's empty or outdated
                                if (existingUUID == null || !cookieStr.contains(existingUUID)) {
                                    cm.setCookie(PrebidServerSettings.COOKIE_DOMAIN, cookieStr);
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                        // CookieSyncManager is deprecated in API 21 Lollipop
                                        CookieSyncManager.createInstance(PrebidMobile.getApplicationContext());
                                        CookieSyncManager csm = CookieSyncManager.getInstance();
                                        if (csm == null) {
                                            LogUtil.i("Unable to find a CookieSyncManager");
                                            return;
                                        }
                                        csm.sync();
                                    } else {
                                        cm.flush();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IllegalStateException ise) {
            } catch (Exception e) {
            }
        }

        private String getExistingCookie() {
            try {
                CookieSyncManager.createInstance(PrebidMobile.getApplicationContext());
                CookieManager cm = CookieManager.getInstance();
                if (cm != null) {
                    String wvcookie = cm.getCookie(PrebidServerSettings.COOKIE_DOMAIN);
                    if (!TextUtils.isEmpty(wvcookie)) {
                        String[] existingCookies = wvcookie.split("; ");
                        for (String cookie : existingCookies) {
                            if (cookie != null && cookie.contains(PrebidServerSettings.AN_UUID)) {
                                return cookie;
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
            return null;
        }


        private JSONObject getPostData() {
            Context context = PrebidMobile.getApplicationContext();
            if (context != null) {
                AdvertisingIDUtil.retrieveAndSetAAID(context);
                PrebidServerSettings.update(context);
            }
            JSONObject postData = new JSONObject();
            try {
                String id = UUID.randomUUID().toString();
                postData.put("id", id);
                JSONObject source = new JSONObject();
                source.put("tid", id);
                postData.put("source", source);
                // add ad units
                JSONArray imp = getImp();
                if (imp != null && imp.length() > 0) {
                    postData.put("imp", imp);
                }
                // add device
                JSONObject device = getDeviceObject();
                if (device != null && device.length() > 0) {
                    postData.put(PrebidServerSettings.REQUEST_DEVICE, device);
                }
                // add app
                JSONObject app = getAppObject();
                if (device != null && device.length() > 0) {
                    postData.put(PrebidServerSettings.REQUEST_APP, app);
                }
                // add user
                JSONObject user = getUserObject();
                if (user != null && user.length() > 0) {
                    postData.put(PrebidServerSettings.REQUEST_USER, user);
                }
                // add regs
                JSONObject regs = getRegsObject();
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
                JSONObject cache = new JSONObject();
                JSONObject bids = new JSONObject();
                cache.put("bids", bids);
                prebid.put("cache", cache);
                JSONObject storedRequest = new JSONObject();
                storedRequest.put("id", PrebidMobile.getAccountId());
                prebid.put("storedrequest", storedRequest);
                JSONObject targetingEmpty = new JSONObject();
                prebid.put("targeting", targetingEmpty);
                ext.put("prebid", prebid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ext;
        }


        private JSONArray getImp() {
            JSONArray impConfigs = new JSONArray();
            // takes information from the ad units
            // look up the configuration of the ad unit
            try {
                JSONObject imp = new JSONObject();
                JSONObject ext = new JSONObject();
                imp.put("id", "PrebidMobile");
                imp.put("secure", 1);
                if (requestParams.getAdType().equals(AdType.INTERSTITIAL)) {
                    imp.put("instl", 1);
                } else {
                    JSONObject banner = new JSONObject();
                    JSONArray format = new JSONArray();
                    for (AdSize size : requestParams.getAdSizes()) {
                        format.put(new JSONObject().put("w", size.getWidth()).put("h", size.getHeight()));
                    }
                    banner.put("format", format);
                    imp.put("banner", banner);
                }
                imp.put("ext", ext);
                JSONObject prebid = new JSONObject();
                ext.put("prebid", prebid);
                JSONObject storedrequest = new JSONObject();
                prebid.put("storedrequest", storedrequest);
                storedrequest.put("id", requestParams.getConfigId());
                imp.put("ext", ext);

                impConfigs.put(imp);
            } catch (JSONException e) {
            }

            return impConfigs;
        }

        private JSONObject getDeviceObject() {
            JSONObject device = new JSONObject();
            try {
                // Device make
                if (!TextUtils.isEmpty(PrebidServerSettings.deviceMake))
                    device.put(PrebidServerSettings.REQUEST_DEVICE_MAKE, PrebidServerSettings.deviceMake);
                // Device model
                if (!TextUtils.isEmpty(PrebidServerSettings.deviceModel))
                    device.put(PrebidServerSettings.REQUEST_DEVICE_MODEL, PrebidServerSettings.deviceModel);
                // Default User Agent
                if (!TextUtils.isEmpty(PrebidServerSettings.userAgent)) {
                    device.put(PrebidServerSettings.REQUEST_USERAGENT, PrebidServerSettings.userAgent);
                }
                // limited ad tracking
                device.put(PrebidServerSettings.REQUEST_LMT, AdvertisingIDUtil.isLimitAdTracking() ? 1 : 0);
                if (!AdvertisingIDUtil.isLimitAdTracking() && !TextUtils.isEmpty(AdvertisingIDUtil.getAAID())) {
                    // put ifa
                    device.put(PrebidServerSettings.REQUEST_IFA, AdvertisingIDUtil.getAAID());
                }

                // os
                device.put(PrebidServerSettings.REQUEST_OS, PrebidServerSettings.os);
                device.put(PrebidServerSettings.REQUEST_OS_VERSION, String.valueOf(Build.VERSION.SDK_INT));
                // language
                if (!TextUtils.isEmpty(PrebidServerSettings.language)) {
                    device.put(PrebidServerSettings.REQUEST_LANGUAGE, PrebidServerSettings.language);
                }
                // POST data that requires context
                Context context = PrebidMobile.getApplicationContext();
                if (context != null) {
                    device.put(PrebidServerSettings.REQUEST_DEVICE_WIDTH, context.getResources().getConfiguration().screenWidthDp);
                    device.put(PrebidServerSettings.REQUEST_DEVICE_HEIGHT, context.getResources().getConfiguration().screenHeightDp);

                    device.put(PrebidServerSettings.REQUEST_DEVICE_PIXEL_RATIO, context.getResources().getDisplayMetrics().density);

                    TelephonyManager telephonyManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);
                    // Get mobile country codes
                    if (PrebidServerSettings.getMCC() < 0 || PrebidServerSettings.getMNC() < 0) {
                        String networkOperator = telephonyManager.getNetworkOperator();
                        if (!TextUtils.isEmpty(networkOperator)) {
                            try {
                                PrebidServerSettings.setMCC(Integer.parseInt(networkOperator.substring(0, 3)));
                                PrebidServerSettings.setMNC(Integer.parseInt(networkOperator.substring(3)));
                            } catch (Exception e) {
                                // Catches NumberFormatException and StringIndexOutOfBoundsException
                                PrebidServerSettings.setMCC(-1);
                                PrebidServerSettings.setMNC(-1);
                            }
                        }
                    }
                    if (PrebidServerSettings.getMCC() > 0 && PrebidServerSettings.getMNC() > 0) {
                        device.put(PrebidServerSettings.REQUEST_MCC_MNC, String.format(Locale.ENGLISH, "%d-%d", PrebidServerSettings.getMCC(), PrebidServerSettings.getMNC()));
                    }

                    // Get carrier
                    if (PrebidServerSettings.getCarrierName() == null) {
                        try {
                            PrebidServerSettings.setCarrierName(telephonyManager.getNetworkOperatorName());
                        } catch (SecurityException ex) {
                            // Some phones require READ_PHONE_STATE permission just ignore name
                            PrebidServerSettings.setCarrierName("");
                        }
                    }
                    if (!TextUtils.isEmpty(PrebidServerSettings.getCarrierName()))
                        device.put(PrebidServerSettings.REQUEST_CARRIER, PrebidServerSettings.getCarrierName());

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
                    device.put(PrebidServerSettings.REQUEST_CONNECTION_TYPE, connection_type);

                    // get location
                    // Do we have access to location?
                    if (PrebidMobile.isShareGeoLocation()) {
                        // get available location through Android LocationManager
                        if (context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
                                || context.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
                            Location lastLocation = null;
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
                            JSONObject geo = new JSONObject();
                            if (lastLocation != null) {
                                Double lat = lastLocation.getLatitude();
                                Double lon = lastLocation.getLongitude();
                                geo.put(PrebidServerSettings.REQEUST_GEO_LAT, lat);
                                geo.put(PrebidServerSettings.REQUEST_GEO_LON, lon);
                                Integer locDataPrecision = Math.round(lastLocation.getAccuracy());
                                //Don't report location data from the future
                                Integer locDataAge = (int) Math.max(0, (System.currentTimeMillis() - lastLocation.getTime()));
                                geo.put(PrebidServerSettings.REQUEST_GEO_AGE, locDataAge);
                                geo.put(PrebidServerSettings.REQUEST_GEO_ACCURACY, locDataPrecision);
                                device.put(PrebidServerSettings.REQUEST_GEO, geo);
                            }
                        } else {
                            LogUtil.w("Location permissions ACCESS_COARSE_LOCATION and/or ACCESS_FINE_LOCATION aren\\'t set in the host app. This may affect demand.");
                        }
                    }
                }
            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getDeviceObject() " + e.getMessage());
            }
            return device;
        }

        private JSONObject getAppObject() {
            JSONObject app = new JSONObject();
            try {
                if (!TextUtils.isEmpty(TargetingParams.getBundleName())) {
                    app.put("bundle", TargetingParams.getBundleName());
                }
                if (!TextUtils.isEmpty(PrebidServerSettings.pkgVersion)) {
                    app.put("ver", PrebidServerSettings.pkgVersion);
                }
                if (!TextUtils.isEmpty(PrebidServerSettings.appName)) {
                    app.put("name", PrebidServerSettings.appName);
                }
                if (!TextUtils.isEmpty(TargetingParams.getDomain())) {
                    app.put("domain", TargetingParams.getDomain());
                }
                if (!TextUtils.isEmpty(TargetingParams.getStoreUrl())) {
                    app.put("storeurl", TargetingParams.getStoreUrl());
                }
                app.put("privacypolicy", TargetingParams.getPrivacyPolicy());
                JSONObject publisher = new JSONObject();
                publisher.put("id", PrebidMobile.getAccountId());
                app.put("publisher", publisher);
                JSONObject prebid = new JSONObject();
                prebid.put("source", "prebid-mobile");
                prebid.put("version", PrebidServerSettings.sdk_version);
                JSONObject ext = new JSONObject();
                ext.put("prebid", prebid);
                app.put("ext", ext);
            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getAppObject() " + e.getMessage());
            }
            return app;

        }

        private JSONObject getUserObject() {
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
                ArrayList<String> keywords = this.requestParams.getKeywords();
                for (String key : keywords) {
                    builder.append(key).append(",");
                }
                String finalKeywords = builder.toString();
                if (!TextUtils.isEmpty(finalKeywords)) {
                    user.put("keywords", finalKeywords);
                }
                if (TargetingParams.isSubjectToGDPR(PrebidMobile.getApplicationContext()) != null) {
                    JSONObject ext = new JSONObject();
                    ext.put("consent", TargetingParams.getGDPRConsentString(PrebidMobile.getApplicationContext()));
                    user.put("ext", ext);
                }
            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getUserObject() " + e.getMessage());
            }
            return user;
        }

        private JSONObject getRegsObject() {
            JSONObject regs = new JSONObject();
            try {
                JSONObject ext = new JSONObject();
                if (TargetingParams.isSubjectToGDPR(PrebidMobile.getApplicationContext()) != null) {
                    if (TargetingParams.isSubjectToGDPR(PrebidMobile.getApplicationContext())) {
                        ext.put("gdpr", 1);
                    } else {
                        ext.put("gdpr", 0);
                    }
                }
                regs.put("ext", ext);
            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getRegsObject() " + e.getMessage());
            }
            return regs;
        }
    }
}
