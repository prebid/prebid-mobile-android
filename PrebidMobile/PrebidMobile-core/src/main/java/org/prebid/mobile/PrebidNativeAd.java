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
import android.view.View;
import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrebidNativeAd {

    private static final String TAG = "PrebidNativeAd";

    private final ArrayList<NativeTitle> titles = new ArrayList<>();
    private final ArrayList<NativeImage> images = new ArrayList<>();
    private final ArrayList<NativeData> dataList = new ArrayList<>();
    private String clickUrl;
    private ArrayList<String> imp_trackers;
    private VisibilityDetector visibilityDetector;
    private boolean expired;
    private View registeredView;
    private PrebidNativeAdEventListener listener;
    private ArrayList<ImpressionTracker> impressionTrackers;


    public static PrebidNativeAd create(String cacheId) {
        String content = CacheManager.get(cacheId);
        if (!TextUtils.isEmpty(content)) {
            try {
                JSONObject details = new JSONObject(content);
                String admStr = details.getString("adm");
                JSONObject adm = new JSONObject(admStr);
                JSONArray asset = adm.getJSONArray("assets");
                final PrebidNativeAd ad = new PrebidNativeAd();
                CacheManager.registerCacheExpiryListener(cacheId, new CacheManager.CacheExpiryListener() {
                    @Override
                    public void onCacheExpired() {
                        if (ad.registeredView == null) {
                            if (ad.listener != null) {
                                ad.listener.onAdExpired();
                            }
                            ad.expired = true;
                            if (ad.visibilityDetector != null) {
                                ad.visibilityDetector.destroy();
                                ad.visibilityDetector = null;
                            }
                            ad.impressionTrackers = null;
                            ad.listener = null;
                        }
                    }
                });
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
                return ad;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    /**
     * This API is used to register the view for Ad Events (#onAdClicked(), #onAdImpression, #onAdExpired)
     *
     * @param view
     * @param listener
     */
    public boolean registerView(View view, final PrebidNativeAdEventListener listener) {
        if (!expired && view != null) {
            this.listener = listener;
            visibilityDetector = VisibilityDetector.create(view);
            if (visibilityDetector == null) {
                return false;
            }

            impressionTrackers = new ArrayList<ImpressionTracker>(imp_trackers.size());
            for (String url : imp_trackers) {
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
            this.registeredView = view;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick(v, listener);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * This API is used to register a list of views for Ad Events (#onAdClicked(), #onAdImpression, #onAdExpired)
     *
     * @param container
     * @param viewList
     * @param listener
     */
    public boolean registerViewList(View container, List<View> viewList, final PrebidNativeAdEventListener listener) {
        if (container == null || viewList == null || viewList.isEmpty()) {
            return false;
        }
        if (!expired && container != null) {
            this.listener = listener;
            visibilityDetector = VisibilityDetector.create(container);
            if (visibilityDetector == null) {
                return false;
            }

            impressionTrackers = new ArrayList<ImpressionTracker>(imp_trackers.size());
            for (String url : imp_trackers) {
                ImpressionTracker impressionTracker = ImpressionTracker.create(url, visibilityDetector, container.getContext(), new ImpressionTrackerListener() {
                    @Override
                    public void onImpressionTrackerFired() {
                        if (listener != null) {
                            listener.onAdImpression();
                        }
                    }
                });
                impressionTrackers.add(impressionTracker);
            }
            this.registeredView = container;

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick(v, listener);
                }
            });

            if (viewList != null && viewList.size() > 0) {
                for (View views : viewList) {
                    views.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handleClick(v, listener);
                        }
                    });
                }
            }
            return true;
        }
        return false;
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
            return true;
        }
        return false;
    }

    private boolean openNativeIntent(String url, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
