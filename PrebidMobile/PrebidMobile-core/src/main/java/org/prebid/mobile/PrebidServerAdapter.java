/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.http.HTTPPost;
import org.prebid.mobile.http.NoContextException;
import org.prebid.mobile.http.TaskResult;
import org.prebid.mobile.tasksmanager.TasksManager;
import org.prebid.mobile.unification.BaseAdUnitConfigurationInterface;
import org.prebid.mobile.unification.NativeAdUnitConfiguration;

import java.lang.ref.WeakReference;
import java.util.*;

class PrebidServerAdapter implements DemandAdapter {

    public CacheIdSaver cacheIdSaver = new CacheIdSaver();
    private ArrayList<ServerConnector> serverConnectors;

    PrebidServerAdapter() {
        serverConnectors = new ArrayList<>();
    }

    @Override
    public void requestDemand(BaseAdUnitConfigurationInterface configuration, DemandAdapterListener listener, String auctionId) {
        final ServerConnector connector = new ServerConnector(this, listener, configuration, auctionId, cacheIdSaver);
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

    static class ServerConnector extends HTTPPost {

        private static final int TIMEOUT_COUNT_DOWN_INTERVAL = 500;

        private final WeakReference<PrebidServerAdapter> prebidServerAdapter;
        private final TimeoutCountDownTimer timeoutCountDownTimer;

        private final BaseAdUnitConfigurationInterface configuration;
        private final String auctionId;

        private DemandAdapterListener listener;
        private boolean timeoutFired;

        private final AdType adType;
        private boolean isCancelled;
        private boolean alreadyPostedResult = false;

        private CacheIdSaver cacheIdSaver;

        ServerConnector(PrebidServerAdapter prebidServerAdapter, DemandAdapterListener listener, BaseAdUnitConfigurationInterface configuration, String auctionId, CacheIdSaver cacheIdSaver) {
            this.prebidServerAdapter = new WeakReference<>(prebidServerAdapter);
            this.listener = listener;
            this.configuration = configuration;
            this.auctionId = auctionId;
            this.cacheIdSaver = cacheIdSaver;
            timeoutCountDownTimer = new TimeoutCountDownTimer(PrebidMobile.getTimeoutMillis(), TIMEOUT_COUNT_DOWN_INTERVAL);
            adType = configuration.getAdType();
        }

        @MainThread
        @Override
        protected void onPostExecute(TaskResult<JSONObject> response) {
            processResult(response);
        }

        public void execute() {
            timeoutCountDownTimer.start();
            super.execute();
        }

        @Override
        protected String getUrl() {
            return PrebidMobile.getPrebidServerHost().getHostUrl();
        }

        @Override
        protected void setTimeoutMillisUpdated(boolean b) {
            PrebidMobile.timeoutMillisUpdated = b;
        }

        @Override
        protected boolean isTimeoutMillisUpdated() {
            return PrebidMobile.timeoutMillisUpdated;
        }

        private void processResult(TaskResult<JSONObject> taskResult) {
            timeoutCountDownTimer.cancel();

            if (taskResult.getError() != null) {
                taskResult.getError().printStackTrace();

                //Default error
                notifyDemandFailed(ResultCode.PREBID_SERVER_ERROR);

                removeThisTask();
                return;
            } else if (taskResult.getResultCode() != null) {
                notifyDemandFailed(taskResult.getResultCode());

                removeThisTask();
                return;
            }

            JSONObject jsonObject = taskResult.getResult();

            HashMap<String, String> keywords = new HashMap<>();
            boolean containTopBid = false;
            if (jsonObject != null) {
                LogUtil.d("Getting response for auction " + getAuctionId() + ": " + jsonObject.toString());
                try {
                    JSONArray seatbid = jsonObject.getJSONArray("seatbid");
                    if (seatbid != null) {
                        for (int i = 0; i < seatbid.length(); i++) {
                            JSONObject seat = seatbid.getJSONObject(i);
                            JSONArray bids = seat.getJSONArray("bid");
                            if (bids != null) {
                                for (int j = 0; j < bids.length(); j++) {
                                    JSONObject bid = bids.getJSONObject(j);
                                    JSONObject hb_key_values = null;
                                    try {
                                        hb_key_values = bid.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting");
                                    } catch (JSONException e) {
                                        // this can happen if lower bids exist on the same seat
                                    }
                                    if (hb_key_values != null) {
                                        Iterator it = hb_key_values.keys();
                                        boolean containBids = false;
                                        while (it.hasNext()) {
                                            String key = (String) it.next();
                                            if (key.equals("hb_cache_id")) {
                                                containTopBid = true;
                                            }
                                            if (key.startsWith("hb_cache_id")) {
                                                containBids = true;
                                            }
                                        }
                                        it = hb_key_values.keys();
                                        if (containBids) {
                                            while (it.hasNext()) {
                                                String key = (String) it.next();
                                                keywords.put(key, hb_key_values.getString(key));
                                            }
                                            // Caching the response only for Native
                                            if (bid.getJSONObject("ext").getJSONObject("prebid").getString("type").equalsIgnoreCase("native")) {
                                                String cacheId = CacheManager.save(bid.toString());
                                                if (cacheId != null) {
                                                    this.cacheIdSaver.setCacheId(cacheId);
                                                    if (bid.has("exp")) {
                                                        Long exp = bid.optLong("exp");
                                                        CacheManager.setExpiry(cacheId, exp);
                                                    }
                                                    keywords.put("hb_cache_id_local", cacheId);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    LogUtil.e("Error processing JSON response.");
                }
            }

            if (!keywords.isEmpty() && containTopBid) {
                notifyContainsTopBid(true);
                notifyDemandReady(keywords);
            } else {
                notifyContainsTopBid(false);
                notifyDemandFailed(ResultCode.NO_BIDS);
            }

            removeThisTask();
        }

        private void cancel() {
            isCancelled = true;
            if (timeoutFired) {
                TasksManager.getInstance().executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDemandFailed(ResultCode.TIMEOUT);
                    }
                });
            } else {
                timeoutCountDownTimer.cancel();
            }
        }
        
        private void removeThisTask() {
            @Nullable
            PrebidServerAdapter prebidServerAdapter = this.prebidServerAdapter.get();
            if (prebidServerAdapter == null) {
                return;
            }

            prebidServerAdapter.serverConnectors.remove(this);
        }

        protected String getAuctionId() {
            return auctionId;
        }

        void destroy() {
            this.cancel();
            this.listener = null;
        }

        @MainThread
        void notifyDemandReady(HashMap<String, String> keywords) {
            if (this.listener == null) {
                return;
            }

            if (!alreadyPostedResult) {
                alreadyPostedResult = true;
                listener.onDemandReady(keywords, getAuctionId());
            }
        }

        @MainThread
        void notifyDemandFailed(ResultCode code) {
            if (this.listener == null) {
                return;
            }

            if (!alreadyPostedResult) {
                alreadyPostedResult = true;
                listener.onDemandFailed(code, getAuctionId());
            }
        }

        private void notifyContainsTopBid(boolean contains) {
            BidLog.BidLogEntry entry = BidLog.getInstance().getLastBid();
            if (entry != null) {
                entry.setContainsTopBid(contains);
            }
        }

        /**
         * Synchronize the uuid2 cookie to the Webview Cookie Jar
         * This is only done if there is no present cookie.
         *
         * @param headers headers to extract cookies from for syncing
         */
        @SuppressWarnings("deprecation")
        protected void httpCookieSync(Map<String, List<String>> headers) {
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

        protected String getExistingCookie() {
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


        protected JSONObject getPostData() throws NoContextException {

            JSONObject postData = new JSONObject();
            try {
                String id = UUID.randomUUID().toString();
                postData.put("id", id);
                // add ad source
                postData.put("source", getSource(id));
                // add ad units
                postData.put("imp", getImp());
                // add device
                postData.put(PrebidServerSettings.REQUEST_DEVICE, getDeviceObject());
                // add app
                postData.put(PrebidServerSettings.REQUEST_APP, getAppObject());
                // add user
                postData.put(PrebidServerSettings.REQUEST_USER, getUserObject());
                // add regs
                postData.put("regs", getRegsObject());
                // add targeting keywords request
                postData.put("ext", getRequestExtData());

                if (PrebidMobile.getPbsDebug()) {
                    postData.put("test", 1);
                }

                JSONObject objectWithoutEmptyValues = Util.getObjectWithoutEmptyValues(postData);

                if (objectWithoutEmptyValues != null) {
                    postData = objectWithoutEmptyValues;

                    JSONObject prebid = postData.getJSONObject("ext").getJSONObject("prebid");

                    JSONObject cache = new JSONObject();
                    JSONObject bids = new JSONObject();
                    cache.put("bids", bids);

                    if (adType.equals(AdType.VIDEO) || adType.equals(AdType.VIDEO_INTERSTITIAL) || adType.equals(AdType.REWARDED_VIDEO)) {
                        cache.put("vastxml", bids);
                    }

                    prebid.put("cache", cache);

                    JSONObject targetingEmpty = new JSONObject();
                    prebid.put("targeting", targetingEmpty);
                }

            } catch (JSONException e) {
            }

            return postData;
        }

        private JSONObject getSource(String id) throws JSONException {
            JSONObject source = new JSONObject();
            source.put("tid", id);
            JSONObject ext = new JSONObject();
            ext.put("omidpn", TargetingParams.getOmidPartnerName());
            ext.put("omidpv", TargetingParams.getOmidPartnerVersion());
            source.put("ext", ext);

            return source;
        }
        private JSONObject getRequestExtData() {
            JSONObject ext = new JSONObject();
            JSONObject prebid = new JSONObject();
            try {
                JSONObject storedRequest = new JSONObject();
                storedRequest.put("id", PrebidMobile.getPrebidServerAccountId());
                prebid.put("storedrequest", storedRequest);

                JSONObject data = new JSONObject().put("bidders", new JSONArray(TargetingParams.getAccessControlList()));
                prebid.put("data", data);
                ext.put("prebid", prebid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ext;
        }

        private JSONArray getImp() throws NoContextException {
            JSONArray impConfigs = new JSONArray();
            // takes information from the ad units
            // look up the configuration of the ad unit
            try {
                JSONObject imp = new JSONObject();
                JSONObject ext = new JSONObject();
                imp.put("id", "PrebidMobile");
                imp.put("secure", 1);
                if (adType.equals(AdType.INTERSTITIAL) || adType.equals(AdType.VIDEO_INTERSTITIAL) || adType.equals(AdType.REWARDED_VIDEO)) {
                    imp.put("instl", 1);
                }

                if (adType.equals(AdType.BANNER) || adType.equals(AdType.INTERSTITIAL)) {

                    JSONObject banner = new JSONObject();
                    JSONArray format = new JSONArray();

                    if (adType.equals(AdType.BANNER)) {
                        for (AdSize size : configuration.castToOriginal().getSizes()) {
                            format.put(new JSONObject().put("w", size.getWidth()).put("h", size.getHeight()));
                        }
                    } else if (adType.equals(AdType.INTERSTITIAL)) {
                        Context context = PrebidMobile.getApplicationContext();
                        if (context != null) {
                            format.put(new JSONObject().put("w", context.getResources().getConfiguration().screenWidthDp).put("h", context.getResources().getConfiguration().screenHeightDp));
                        } else {
                            // Unlikely this is being called, if so, please check if you've set up the SDK properly
                            throw new NoContextException();
                        }
                    }

                    banner.put("format", format);

                    BannerBaseAdUnit.Parameters parameters = configuration.castToOriginal().getBannerParameters();
                    if (parameters != null) {

                        List<Integer> apiList = Util.convertCollection(parameters.getApi(), new Util.Function1<Integer, Signals.Api>() {
                            @Override
                            public Integer apply(Signals.Api element) {
                                return element.value;
                            }
                        });

                        banner.put("api", new JSONArray(apiList));

                    }

                    imp.put("banner", banner);
                } else if (adType.equals(AdType.NATIVE)) {
                    // add native request
                    JSONObject nativeObj = new JSONObject();
                    JSONObject request = new JSONObject();
                    JSONArray assets = new JSONArray();
                    NativeAdUnitConfiguration params = configuration.castToNative();
                    if (params.getContextType() != null) {
                        request.put(NativeRequestParams.CONTEXT, params.getContextType().getID());
                    }
                    if (params.getContextSubtype() != null) {
                        request.put(NativeRequestParams.CONTEXT_SUB_TYPE, params.getContextSubtype().getID());
                    }
                    if (params.getPlacementType() != null) {
                        request.put(NativeRequestParams.PLACEMENT_TYPE, params.getPlacementType().getID());
                    }
                    request.put(NativeRequestParams.PLACEMENT_COUNT, params.getPlacementCount());
                    request.put(NativeRequestParams.SEQ, params.getSeq());
                    request.put(NativeRequestParams.A_URL_SUPPORT, params.getAUrlSupport() ? 1 : 0);
                    request.put(NativeRequestParams.D_URL_SUPPORT, params.getDUrlSupport() ? 1 : 0);
                    if (!params.getNativeEventTrackers().isEmpty()) {
                        JSONArray trackers = new JSONArray();
                        for (NativeEventTracker tracker : params.getNativeEventTrackers()) {
                            JSONObject trackerObject = new JSONObject();
                            trackerObject.put(NativeRequestParams.EVENT, tracker.getEvent().getID());
                            JSONArray methodsArray = new JSONArray();
                            for (NativeEventTracker.EVENT_TRACKING_METHOD method : tracker.getMethods()) {
                                methodsArray.put(method.getID());
                            }
                            trackerObject.put(NativeRequestParams.METHODS, methodsArray);
                            trackerObject.put(NativeRequestParams.EXT, tracker.getExtObject());
                            trackers.put(trackerObject);
                        }
                        request.put(NativeRequestParams.EVENT_TRACKERS, trackers);
                    }
                    request.put(NativeRequestParams.PRIVACY, params.getPrivacy() ? 1 : 0);
                    request.put(NativeRequestParams.EXT, params.getExt());
                    if (!params.getNativeAssets().isEmpty()) {
                        int idCount = 1;
                        for (NativeAsset asset : params.getNativeAssets()) {
                            JSONObject assetObj;
                            switch (asset.getType()) {
                                case TITLE:
                                    NativeTitleAsset titleAsset = (NativeTitleAsset) asset;
                                    assetObj = new JSONObject();
                                    JSONObject title = new JSONObject();
                                    title.put(NativeRequestParams.LENGTH, titleAsset.getLen());
                                    if (titleAsset.getTitleExt() != null) {
                                        title.put(NativeRequestParams.EXT, titleAsset.getTitleExt());
                                    }
                                    assetObj.put(NativeRequestParams.TITLE, title);
                                    assetObj.put(NativeRequestParams.REQUIRED, titleAsset.isRequired() ? 1 : 0);
                                    assetObj.put(NativeRequestParams.EXT, titleAsset.getAssetExt());
                                    if (PrebidMobile.shouldAssignNativeAssetID()) {
                                        assetObj.put(NativeRequestParams.ID, idCount++);
                                    }
                                    assets.put(assetObj);
                                    break;
                                case IMAGE:
                                    NativeImageAsset imageAsset = (NativeImageAsset) asset;
                                    assetObj = new JSONObject();
                                    JSONObject image = new JSONObject();
                                    image.put(NativeRequestParams.TYPE, imageAsset.getImageType().getID());
                                    if (imageAsset.getImageExt() != null) {
                                        image.put(NativeRequestParams.EXT, imageAsset.getImageExt());
                                    }
                                    if (imageAsset.getHMin() > 0 && imageAsset.getWMin() > 0) {
                                        image.put(NativeRequestParams.WIDTH_MIN, imageAsset.getWMin());
                                        image.put(NativeRequestParams.HEIGHT_MIN, imageAsset.getHMin());
                                    }
                                    if (imageAsset.getH() > 0 && imageAsset.getW() > 0) {
                                        image.put(NativeRequestParams.WIDTH, imageAsset.getW());
                                        image.put(NativeRequestParams.HEIGHT, imageAsset.getH());
                                    }
                                    if (!imageAsset.getMimes().isEmpty()) {
                                        JSONArray imageMimesArray = new JSONArray();
                                        for (String mime : imageAsset.getMimes()) {
                                            imageMimesArray.put(mime);
                                        }
                                        image.put(NativeRequestParams.MIMES, imageMimesArray);
                                    }
                                    assetObj.put(NativeRequestParams.IMAGE, image);
                                    assetObj.put(NativeRequestParams.REQUIRED, imageAsset.isRequired() ? 1 : 0);
                                    assetObj.put(NativeRequestParams.EXT, imageAsset.getAssetExt());
                                    if (PrebidMobile.shouldAssignNativeAssetID()) {
                                        assetObj.put(NativeRequestParams.ID, idCount++);
                                    }
                                    assets.put(assetObj);
                                    break;
                                case DATA:
                                    NativeDataAsset dataAsset = (NativeDataAsset) asset;
                                    assetObj = new JSONObject();
                                    JSONObject data = new JSONObject();
                                    data.put(NativeRequestParams.TYPE, dataAsset.getDataType().getID());
                                    if (dataAsset.getLen() > 0) {
                                        data.put(NativeRequestParams.LENGTH, dataAsset.getLen());
                                    }
                                    if (dataAsset.getDataExt() != null) {
                                        data.put(NativeRequestParams.EXT, dataAsset.getDataExt());
                                    }
                                    assetObj.put(NativeRequestParams.DATA, data);
                                    assetObj.put(NativeRequestParams.REQUIRED, dataAsset.isRequired() ? 1 : 0);
                                    assetObj.put(NativeRequestParams.EXT, dataAsset.getAssetExt());
                                    if (PrebidMobile.shouldAssignNativeAssetID()) {
                                        assetObj.put(NativeRequestParams.ID, idCount++);
                                    }
                                    assets.put(assetObj);

                                    break;
                            }
                        }
                    }
                    request.put(NativeRequestParams.ASSETS, assets);
                    request.put(NativeRequestParams.VERSION, NativeRequestParams.SUPPORTED_VERSION);
                    nativeObj.put(NativeRequestParams.REQUEST, request.toString());
                    nativeObj.put(NativeRequestParams.VERSION, NativeRequestParams.SUPPORTED_VERSION);
                    imp.put(NativeRequestParams.NATIVE, nativeObj);
                } else if (adType.equals(AdType.VIDEO) || adType.equals(AdType.VIDEO_INTERSTITIAL) || adType.equals(AdType.REWARDED_VIDEO)) {

                    JSONObject video = new JSONObject();
                    Integer placementValue = null;

                    VideoBaseAdUnit.Parameters parameters = configuration.castToOriginal().getVideoParameters();
                    if (parameters != null) {

                        List<Integer> apiList = Util.convertCollection(parameters.getApi(), new Util.Function1<Integer, Signals.Api>() {
                            @Override
                            public Integer apply(Signals.Api element) {
                                return element.value;
                            }
                        });

                        List<Integer> playbackMethodList = Util.convertCollection(parameters.getPlaybackMethod(), new Util.Function1<Integer, Signals.PlaybackMethod>() {
                            @Override
                            public Integer apply(Signals.PlaybackMethod element) {
                                return element.value;
                            }
                        });

                        List<Integer> protocolList = Util.convertCollection(parameters.getProtocols(), new Util.Function1<Integer, Signals.Protocols>() {
                            @Override
                            public Integer apply(Signals.Protocols element) {
                                return element.value;
                            }
                        });

                        Integer startDelayValue = null;
                        Signals.StartDelay startDelay = parameters.getStartDelay();
                        if (startDelay != null) {
                            startDelayValue = startDelay.value;
                        }

                        Signals.Placement placement = parameters.getPlacement();
                        if (placement != null) {
                            placementValue = placement.value;
                        }

                        video.put("api", new JSONArray(apiList));
                        video.put("maxbitrate", parameters.getMaxBitrate());
                        video.put("minbitrate", parameters.getMinBitrate());
                        video.put("maxduration", parameters.getMaxDuration());
                        video.put("minduration", parameters.getMinDuration());
                        video.put("mimes", new JSONArray(parameters.getMimes()));
                        video.put("playbackmethod", new JSONArray(playbackMethodList));
                        video.put("protocols", new JSONArray(protocolList));
                        video.put("startdelay", startDelayValue);
                    }

                    Integer placementValueDefault = null;
                    if (adType.equals(AdType.VIDEO)) {
                        for (AdSize size : configuration.castToOriginal().getSizes()) {
                            video.put("w", size.getWidth());
                            video.put("h", size.getHeight());
                        }

                    } else if (adType.equals(AdType.VIDEO_INTERSTITIAL) || adType.equals(AdType.REWARDED_VIDEO)) {
                        Context context = PrebidMobile.getApplicationContext();

                        if (context != null) {
                            video.put("w", context.getResources().getConfiguration().screenWidthDp);
                            video.put("h", context.getResources().getConfiguration().screenHeightDp);
                        }

                        placementValueDefault = 5;
                    }

                    if (placementValue == null) {
                        placementValue = placementValueDefault;
                    }

                    video.put("placement", placementValue);

                    video.put("linearity", 1);

                    imp.put("video", video);
                }

                JSONObject prebid = new JSONObject();
                ext.put("prebid", prebid);
                JSONObject context = new JSONObject();

                JSONObject contextData = Util.toJson(configuration.getContextDataDictionary());
                JSONObject data = new JSONObject(contextData.toString());
                data.put("adslot", configuration.getPbAdSlot());

                context.put("data", data);
                context.put("keywords", TextUtils.join(",", configuration.getContextKeywordsSet()));
                ext.put("context", context);
                JSONObject storedrequest = new JSONObject();
                prebid.put("storedrequest", storedrequest);
                storedrequest.put("id", configuration.getConfigId());

                if (!TextUtils.isEmpty(PrebidMobile.getStoredAuctionResponse())) {
                    JSONObject storedAuctionResponse = new JSONObject();
                    prebid.put("storedauctionresponse", storedAuctionResponse);
                    storedAuctionResponse.put("id", PrebidMobile.getStoredAuctionResponse());
                }

                if (!PrebidMobile.getStoredBidResponses().isEmpty()) {
                    JSONArray bidResponseArray = new JSONArray();
                    prebid.put("storedbidresponse", bidResponseArray);

                    for (String bidder : PrebidMobile.getStoredBidResponses().keySet()) {
                        String bidId = PrebidMobile.getStoredBidResponses().get(bidder);
                        if (!TextUtils.isEmpty(bidder) && !TextUtils.isEmpty(bidId)) {
                            JSONObject storedBid = new JSONObject();
                            storedBid.put("bidder", bidder);
                            storedBid.put("id", bidId);
                            bidResponseArray.put(storedBid);
                        }
                    }
                }

                if (adType.equals(AdType.REWARDED_VIDEO)) {
                    prebid.put("is_rewarded_inventory", 1);
                }

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
                if(canIAccessDeviceData()) {
                    if (!AdvertisingIDUtil.isLimitAdTracking() && !TextUtils.isEmpty(AdvertisingIDUtil.getAAID())) {
                        // put ifa
                        device.put(PrebidServerSettings.REQUEST_IFA, AdvertisingIDUtil.getAAID());
                    }
                }

                // os
                device.put(PrebidServerSettings.REQUEST_OS, PrebidServerSettings.os);
                device.put(PrebidServerSettings.REQUEST_OS_VERSION, String.valueOf(Build.VERSION.SDK_INT));
                // language
                if (!TextUtils.isEmpty(Locale.getDefault().getLanguage())) {
                    device.put(PrebidServerSettings.REQUEST_LANGUAGE, Locale.getDefault().getLanguage());
                }

                if (adType.equals(AdType.INTERSTITIAL)) {

                    Integer minSizePercWidth = null;
                    Integer minSizePercHeight = null;

                    AdSize minSizePerc = configuration.castToOriginal().getMinSizePercentage();
                    if (minSizePerc != null) {

                        minSizePercWidth = minSizePerc.getWidth();
                        minSizePercHeight = minSizePerc.getHeight();
                    }

                    JSONObject deviceExt = new JSONObject();
                    JSONObject deviceExtPrebid = new JSONObject();
                    JSONObject deviceExtPrebidInstl = new JSONObject();

                    device.put("ext", deviceExt);
                    deviceExt.put("prebid", deviceExtPrebid);
                    deviceExtPrebid.put("interstitial", deviceExtPrebidInstl);
                    deviceExtPrebidInstl.put("minwidthperc", minSizePercWidth);
                    deviceExtPrebidInstl.put("minheightperc", minSizePercHeight);

                    device.put("ext", deviceExt);
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
                putContentObjectIfExists(app);

                JSONObject publisher = new JSONObject();
                publisher.put("id", PrebidMobile.getPrebidServerAccountId());
                app.put("publisher", publisher);
                JSONObject prebid = new JSONObject();
                prebid.put("source", "prebid-mobile");
                prebid.put("version", PrebidServerSettings.sdk_version);
                JSONObject ext = new JSONObject();
                ext.put("prebid", prebid);
                ext.put("data", Util.toJson(TargetingParams.getContextDataDictionary()));
                app.put("ext", ext);
                app.put("keywords", TextUtils.join(",", TargetingParams.getContextKeywordsSet()));
            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getAppObject() " + e.getMessage());
            }
            return app;

        }

        private void putContentObjectIfExists(JSONObject appJsonObject) {
            ContentObject contentObject = configuration.getAppContent();
            if (contentObject == null) return;

            JSONObject jsonContentObject = contentObject.getJsonObject();
            if (jsonContentObject == null) return;

            try {
                appJsonObject.put("content", jsonContentObject);
            } catch (Exception any) {
                LogUtil.e("PrebidServerAdapter", "Can't create content json object!");
            }
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

                String globalUserKeywordString = TextUtils.join(",", TargetingParams.getUserKeywordsSet());
                user.put("keywords", globalUserKeywordString);

                JSONObject ext = new JSONObject();

                Boolean isSubjectToGDPR = TargetingParams.isSubjectToGDPR();
                if (Boolean.TRUE.equals(isSubjectToGDPR)) {
                    ext.put("consent", TargetingParams.getGDPRConsentString());
                }

                ArrayList<DataObject> userDataObjects = configuration.getUserData();
                if (!userDataObjects.isEmpty()) {
                    JSONArray userDataJsonArray = new JSONArray();
                    for (DataObject dataObject : userDataObjects) {
                        userDataJsonArray.put(dataObject.getJsonObject());
                    }
                    user.put("data", userDataJsonArray);
                }

                ext.put("data", Util.toJson(TargetingParams.getUserDataDictionary()));

                ext.put("eids", getExternalUserIdArray());

                user.put("ext", ext);

            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getUserObject() " + e.getMessage());
            }
            return user;
        }

        private JSONArray getExternalUserIdArray() {
            JSONArray transformedUserIdArray = new JSONArray();
            List<ExternalUserId> externalUserIds = PrebidMobile.getExternalUserIds();
            if (externalUserIds == null || externalUserIds.isEmpty()) {
                externalUserIds = TargetingParams.fetchStoredExternalUserIds();
            }
            try {
                if (externalUserIds != null && !externalUserIds.isEmpty()) {
                    for (ExternalUserId externaluserId : externalUserIds) {
                        if (externaluserId.getSource() == null || externaluserId.getSource().length() == 0 || externaluserId.getIdentifier() == null || externaluserId.getIdentifier().length() == 0) {
                            return null;
                        }
                        JSONObject transformedUserIdObject = new JSONObject();
                        transformedUserIdObject.put("source", externaluserId.getSource());
                        JSONArray uidArray = new JSONArray();
                        JSONObject uidObject = new JSONObject();
                        uidObject.put("id", externaluserId.getIdentifier());
                        if (externaluserId.getAtype() != null)
                        {
                            uidObject.put("atype", externaluserId.getAtype());
                        }
                        if (externaluserId.getExt() != null)
                        {
                            JSONObject extObject = new JSONObject(externaluserId.getExt());
                            uidObject.put("ext", extObject);
                        }
                        uidArray.put(uidObject);
                        transformedUserIdObject.put("uids", uidArray);
                        transformedUserIdArray.put(transformedUserIdObject);
                    }
                }
            }catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getExternalUserIdArray() " + e.getMessage());
            }

            return transformedUserIdArray;
        }

        private JSONObject getRegsObject() {
            JSONObject regs = new JSONObject();
            try {
                JSONObject ext = new JSONObject();
                Boolean isSubjectToGDPR = TargetingParams.isSubjectToGDPR();

                if (TargetingParams.isSubjectToCOPPA()) {
                    regs.put("coppa", 1);
                }

                if (Boolean.TRUE.equals(isSubjectToGDPR)) {
                    ext.put("gdpr", 1);

                }

                ext.put("us_privacy", StorageUtils.getIabCcpa());

                regs.put("ext", ext);

            } catch (JSONException e) {
                LogUtil.d("PrebidServerAdapter getRegsObject() " + e.getMessage());
            }
            return regs;
        }

        protected boolean canIAccessDeviceData() {
            //fetch advertising identifier based TCF 2.0 Purpose1 value
            //truth table
            /*
                                 deviceAccessConsent=true   deviceAccessConsent=false  deviceAccessConsent undefined
            gdprApplies=false        Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
            gdprApplies=true         Yes, read IDFA             No, don’t read IDFA           No, don’t read IDFA
            gdprApplies=undefined    Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
            */

            boolean setDeviceId = false;

            Boolean gdprApplies = TargetingParams.isSubjectToGDPR();
            Boolean deviceAccessConsent = TargetingParams.getDeviceAccessConsent();

            if((deviceAccessConsent == null && (gdprApplies == null || Boolean.FALSE.equals(gdprApplies)))
                    || Boolean.TRUE.equals(deviceAccessConsent)) {

                setDeviceId = true;
            }

            return setDeviceId;
        }

        class TimeoutCountDownTimer extends CountDownTimer {

            /**
             * @param millisInFuture    The number of millis in the future from the call
             *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
             *                          is called.
             * @param countDownInterval The interval along the way to receive
             *                          {@link #onTick(long)} callbacks.
             */
            public TimeoutCountDownTimer(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);

            }

            @Override
            public void onTick(long millisUntilFinished) {
                if (isCancelled) {
                    TimeoutCountDownTimer.this.cancel();
                }
            }

            @Override
            public void onFinish() {

                if (isCancelled) {
                    return;
                }

                timeoutFired = true;
                ServerConnector.this.cancel();

            }
        }
    }

    class CacheIdSaver {

        private String cacheId;

        public void setCacheId(String cacheId) {
            this.cacheId = cacheId;
        }

        public String getCacheId() {
            return cacheId;
        }

    }
}
