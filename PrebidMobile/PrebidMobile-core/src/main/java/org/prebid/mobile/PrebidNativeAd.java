/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.bidding.events.EventsNotifier;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Response native ad object for all assets.
 */
public class PrebidNativeAd {

    private static final String TAG = "PrebidNativeAd";

    private boolean impressionIsNotNotified = true;

    private final ArrayList<NativeTitle> titles = new ArrayList<>();
    private final ArrayList<NativeImage> images = new ArrayList<>();
    private final ArrayList<NativeData> dataList = new ArrayList<>();
    private String clickUrl;
    @Nullable
    private ArrayList<String> imp_trackers;
    @Nullable
    private ArrayList<String> click_trackers;
    private VisibilityDetector visibilityDetector;
    private boolean expired;
    private WeakReference<View> registeredView;
    private PrebidNativeAdEventListener listener;
    private ArrayList<ImpressionTracker> impressionTrackers;
    private ArrayList<ClickTracker> clickTrackers;
    private String winEvent;
    private String impEvent;
    @Nullable
    private String privacyUrl;


    public static PrebidNativeAd create(String cacheId) {
        String content = CacheManager.get(cacheId);
        if (!TextUtils.isEmpty(content)) {
            try {
                JSONObject details = new JSONObject(content);
                String admStr = details.getString("adm");
                JSONObject adm = new JSONObject(admStr);
                JSONArray asset = adm.getJSONArray("assets");
                final PrebidNativeAd ad = new PrebidNativeAd();
                CacheManager.registerCacheExpiryListener(cacheId, new CacheExpireListenerImpl(ad));
                for (int i = 0; i < asset.length(); i++) {
                    JSONObject adObject = asset.getJSONObject(i);
                    if (adObject.has("title")) {
                        JSONObject title = adObject.getJSONObject("title");
                        if (title.has("text")) {
                            String titleText = title.getString("text");
                            if (!titleText.isEmpty()) {
                                ad.addTitle(new NativeTitle(titleText));
                            }
                        } else {
                            LogUtil.warning(TAG, "Json title object doesn't have text field");
                        }
                    }
                    if (adObject.has("data")) {
                        JSONObject data = adObject.getJSONObject("data");

                        if (data.has("value")) {
                            int type = 0;
                            if (data.has("type")) {
                                type = data.optInt("type");
                            }
                            String value = data.getString("value");
                            ad.addData(new NativeData(type, value));
                        } else {
                            LogUtil.warning(TAG, "Json data object doesn't have type or value field");
                        }
                    }

                    if (adObject.has("img")) {
                        JSONObject img = adObject.getJSONObject("img");
                        if (img.has("url")) {
                            int type = 0;
                            if (img.has("type")) {
                                type = img.optInt("type");
                            }
                            String url = img.getString("url");
                            ad.addImage(new NativeImage(type, url));
                        } else {
                            LogUtil.warning(TAG, "Json image object doesn't have url or type field");
                        }
                    }
                }

                if (adm.has("link")) {
                    JSONObject link = adm.getJSONObject("link");
                    if (link.has("url")) {
                        String url = link.getString("url");
                        if (url.contains("{AUCTION_PRICE}") && details.has("price")) {
                            url = url.replace("{AUCTION_PRICE}", details.getString("price"));
                        }
                        ad.setClickUrl(url);
                    }

                    if (link.has("clicktrackers")) {
                        JSONArray clicktrackers = link.getJSONArray("clicktrackers");
                        if (clicktrackers.length() > 0) {
                            ad.click_trackers = new ArrayList<>();
                            for (int count = 0; count < clicktrackers.length(); count++) {
                                String clickTrackerUrl = clicktrackers.getString(count);
                                if (clickTrackerUrl.contains("{AUCTION_PRICE}") && details.has("price")) {
                                    clickTrackerUrl = clickTrackerUrl.replace("{AUCTION_PRICE}", details.getString("price"));
                                }
                                ad.click_trackers.add(clickTrackerUrl);
                            }
                        }
                    }
                }

                if (adm.has("eventtrackers")) {
                    JSONArray eventtrackers = adm.getJSONArray("eventtrackers");
                    if (eventtrackers.length() > 0) {
                        ad.imp_trackers = new ArrayList<>();
                        for (int count = 0; count < eventtrackers.length(); count++) {
                            JSONObject eventtracker = eventtrackers.getJSONObject(count);
                            if (eventtracker.has("url")) {
                                String impUrl = eventtracker.getString("url");
                                if (impUrl.contains("{AUCTION_PRICE}") && details.has("price")) {
                                    impUrl = impUrl.replace("{AUCTION_PRICE}", details.getString("price"));
                                }
                                ad.imp_trackers.add(impUrl);
                            }
                        }
                    }
                }

                if (adm.has("privacy")) {
                    String url = adm.getString("privacy");
                    ad.setPrivacyUrl(url);
                }
                parseEvents(details, ad);
                return ad;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void parseEvents(
            JSONObject bidJson,
            PrebidNativeAd ad
    ) {
        ad.winEvent = EventsNotifier.parseEvent("win", bidJson);
        ad.impEvent = EventsNotifier.parseEvent("imp", bidJson);
    }


    private PrebidNativeAd() {
    }

    public void addTitle(NativeTitle title) {
        titles.add(title);
    }

    public void addData(NativeData data) {
        dataList.add(data);
    }

    public void addImage(NativeImage image) {
        images.add(image);
    }

    @NonNull
    public ArrayList<NativeTitle> getTitles() {
        return titles;
    }

    @NonNull
    public ArrayList<NativeImage> getImages() {
        return images;
    }

    @NonNull
    public ArrayList<NativeData> getDataList() {
        return dataList;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    private void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    /**
     * @return First title or empty string if it doesn't exist
     */
    @NonNull
    public String getTitle() {
        if (!titles.isEmpty()) {
            return titles.get(0).getText();
        }
        return "";
    }

    /**
     * @return First description data value or empty string if it doesn't exist
     */
    @NonNull
    public String getDescription() {
        for (NativeData data : dataList) {
            if (data.getType() == NativeData.Type.DESCRIPTION) {
                return data.getValue();
            }
        }
        return "";
    }

    /**
     * @return First icon url or empty string if it doesn't exist
     */
    @NonNull
    public String getIconUrl() {
        for (NativeImage image : images) {
            if (image.getType() == NativeImage.Type.ICON) {
                return image.getUrl();
            }
        }
        return "";
    }

    /**
     * @return First main image url or empty string if it doesn't exist
     */
    @NonNull
    public String getImageUrl() {
        for (NativeImage image : images) {
            if (image.getType() == NativeImage.Type.MAIN_IMAGE) {
                return image.getUrl();
            }
        }
        return "";
    }

    /**
     * @return First call to action data value or empty string if it doesn't exist
     */
    @NonNull
    public String getCallToAction() {
        for (NativeData data : dataList) {
            if (data.getType() == NativeData.Type.CALL_TO_ACTION) {
                return data.getValue();
            }
        }
        return "";
    }

    /**
     * @return First sponsored by data value or empty string if it doesn't exist
     */
    @NonNull
    public String getSponsoredBy() {
        for (NativeData data : dataList) {
            if (data.getType() == NativeData.Type.SPONSORED_BY) {
                return data.getValue();
            }
        }
        return "";
    }

    @Nullable
    public String getPrivacyUrl() {
        return privacyUrl;
    }

    private void setPrivacyUrl(@Nullable String url) {
        privacyUrl = url;
    }

    /**
     * This API is used to register the view for Ad Events (#onAdClicked(), #onAdImpression, #onAdExpired).
     *
     * @param container      the native ad container used to track impression
     * @param clickableViews list of views that should handle click
     * @param listener must not contain any references to View, Activity, because it can be in memory for a long time.
     *                 Should be class implementation and not anonymous object.
     *                 If it is anonymous class it can produce memory leak.
     * @return true if views registered successfully
     */
    public boolean registerView(View container, List<View> clickableViews, final PrebidNativeAdEventListener listener) {
        if (container == null || clickableViews == null || clickableViews.isEmpty()) {
            return false;
        }
        if (!expired && container != null) {
            this.listener = listener;
            visibilityDetector = VisibilityDetector.create(container);
            if (visibilityDetector == null) {
                return false;
            }

            createImpressionTrackers(container);

            registeredView = new WeakReference<>(container);

            container.setOnClickListener(v -> handleClick(v, listener));

            if (clickableViews != null && clickableViews.size() > 0) {
                for (View views : clickableViews) {
                    if (views != null) {
                        views.setOnClickListener(v -> handleClick(v, listener));
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void createImpressionTrackers(View view) {
        ArrayList<String> combinedImpTrackers = new ArrayList<>();
        if (imp_trackers != null) {
            combinedImpTrackers.addAll(imp_trackers);
        }
        if (impEvent != null) {
            combinedImpTrackers.add(impEvent);
        }

        impressionTrackers = new ArrayList<>();
        for (String url : combinedImpTrackers) {
            ImpressionTracker impressionTracker = ImpressionTracker.create(url, visibilityDetector, view.getContext(), new ImpressionTrackerListener() {
                @Override
                public void onImpressionTrackerFired() {
                    if (listener != null) {
                        listener.onAdImpression();
                    }
                }
            });
            impressionTrackers.add(impressionTracker);
        }
    }

    protected boolean registerPrebidNativeAdEventListener(PrebidNativeAdEventListener listener) {
        this.listener = listener;
        return true;
    }

    private boolean handleClick(View v, PrebidNativeAdEventListener listener) {
        if (clickUrl == null || clickUrl.isEmpty()) {
            return false;
        }

        // open browser
        if (openNativeIntent(clickUrl, v.getContext())) {
            if (listener != null) {
                listener.onAdClicked();
            }
            fireClickTrackers(v.getContext());
            return true;
        }
        return false;
    }

    private boolean openNativeIntent(
            String url,
            Context context
    ) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ExternalViewerUtils.startActivity(context, intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public String getWinEvent() {
        return winEvent;
    }

    public String getImpEvent() {
        return impEvent;
    }


    private void notifyImpressionEvent() {
        if (impressionIsNotNotified) {
            impressionIsNotNotified = false;
            EventsNotifier.notify(impEvent);
        }
    }

    private void fireClickTrackers(Context context) {
        if (click_trackers == null) {
            return;
        }
        for (String url: click_trackers) {
            ClickTracker.createAndFire(url, context, null);
        }
    }

    static class CacheExpireListenerImpl implements CacheManager.CacheExpiryListener {

        private PrebidNativeAd ad;

        public CacheExpireListenerImpl(PrebidNativeAd ad) {
            this.ad = ad;
        }

        @Override
        public void onCacheExpired() {
            Log.e(TAG, "onCacheExpired");
            WeakReference<View> weakReference = ad.registeredView;
            if (weakReference == null) return;

            View view = weakReference.get();
            if (view != null) return;

            if (ad.listener != null) {
                ad.listener.onAdExpired();
            }
            ad.expired = true;
            if (ad.visibilityDetector != null) {
                ad.visibilityDetector.destroy();
                ad.visibilityDetector = null;
            }
            ad.impressionTrackers = null;
            ad.clickTrackers = null;
            ad.listener = null;
        }

    }

}
