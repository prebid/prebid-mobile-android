package org.prebid.mobile;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
    public void requestDemand(Context context, RequestParams params, DemandAdapterListener listener, String auctionId) {
        ServerConnector connector = new ServerConnector(context, listener, params, auctionId);
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

    class ServerConnector extends AsyncTask<Object, Object, JSONObject> {
        private WeakReference<Context> contextWeakReference;
        private DemandAdapterListener listener;
        private RequestParams requestParams;
        private String auctionId;

        ServerConnector(Context context, DemandAdapterListener listener, RequestParams requestParams, String auctionId) {
            this.contextWeakReference = new WeakReference<Context>(context);
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
                wr.write(getPostData(this.requestParams).toString());
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
            if (listener != null) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> keywords = new HashMap<>();
                        keywords.put("hb_cache_id", "fake-id");
                        keywords.put("hb_pb", "0.50");
                        listener.onDemandReady(keywords, auctionId);
                    }
                }, 0);
            }
            serverConnectors.remove(this);
        }

        String getAuctionId() {
            return auctionId;
        }

        void destroy() {
            this.cancel(true);
            this.listener = null;
            this.contextWeakReference.clear();
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
                                        Context context = contextWeakReference.get();
                                        CookieSyncManager.createInstance(context);
                                        CookieSyncManager csm = CookieSyncManager.getInstance();
                                        if (csm == null) {
                                            LogUtil.i(PrebidServerSettings.TAG, "Unable to find a CookieSyncManager");
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
                Context context = contextWeakReference.get();
                CookieSyncManager.createInstance(context);
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


        private JSONObject getPostData(RequestParams requestParams) {
            Context context = contextWeakReference.get();
            if (context != null) {
//            AdvertisingIDUtil.retrieveAndSetAAID(context);
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
                JSONArray imp = getImp(requestParams);
                if (imp != null && imp.length() > 0) {
                    postData.put("imp", imp);
                }
                // add device
                JSONObject device = getDeviceObject(context);
                if (device != null && device.length() > 0) {
                    postData.put(PrebidServerSettings.REQUEST_DEVICE, device);
                }
                // add app
                JSONObject app = getAppObject(context);
                if (device != null && device.length() > 0) {
                    postData.put(PrebidServerSettings.REQUEST_APP, app);
                }
                // add user
                // todo should we provide api for developers to pass in user's location (zip, city, address etc, not real time location)
                JSONObject user = getUserObject(requestParams, context);
                if (user != null && user.length() > 0) {
                    postData.put(PrebidServerSettings.REQUEST_USER, user);
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


        private JSONArray getImp(RequestParams requestParams) {
            JSONArray impConfigs = new JSONArray();
            // takes information from the ad units
            // look up the configuration of the ad unit
            try {
                JSONObject imp = new JSONObject();
                JSONObject ext = new JSONObject();
                imp.put("id", "PrebidMobile");
//                if (PrebidMobile.shouldUseSecureConnection()) {
                imp.put("secure", 1);
//                }
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

        private JSONObject getDeviceObject(Context context) {
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
                // POST data that requires context
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
                }
                // Location PrebidServerSettings
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
                        LogUtil.w(PrebidServerSettings.TAG,
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
                    geo.put(PrebidServerSettings.REQEUST_GEO_LAT, lat);
                    geo.put(PrebidServerSettings.REQUEST_GEO_LON, lon);
                    if (locDataAge != null)
                        geo.put(PrebidServerSettings.REQUEST_GEO_AGE, locDataAge);
                    if (locDataPrecision != null)
                        geo.put(PrebidServerSettings.REQUEST_GEO_ACCURACY, locDataPrecision);
                }
                if (geo.length() > 0) {
                    device.put(PrebidServerSettings.REQUEST_GEO, geo);
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
            }
            return app;

        }

        private JSONObject getUserObject(RequestParams requestParams, Context context) {
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
                ArrayList<String> keywords = requestParams.getKeywords();
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
    }
}
