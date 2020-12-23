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
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrebidNativeAd {
    private String title;
    private String description;
    private String iconUrl;
    private String imageUrl;
    private String cta;
    private String sponsoredBy;
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
                            ad.setTitle(title.getString("text"));
                        }
                    }
                    if (adObject.has("data")) {
                        JSONObject data = adObject.getJSONObject("data");
                        if (data.optInt("type") == 1) {
                            ad.setSponsoredBy(data.getString("value"));
                        } else if (data.optInt("type") == 2) {
                            ad.setDescription(data.getString("value"));
                        } else if (data.optInt("type") == 12) {
                            ad.setCallToAction(data.getString("value"));
                        }
                    }

                    if (adObject.has("img")) {
                        if (adObject.optInt("id") == 1) {
                            JSONObject img = adObject.getJSONObject("img");
                            if (img.has("url")) {
                                ad.setIconUrl(img.getString("url"));
                            }
                        } else if (adObject.optInt("id") == 2) {
                            JSONObject img = adObject.getJSONObject("img");
                            if (img.has("url")) {
                                ad.setImageUrl(img.getString("url"));
                            }
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
                                ad.imp_trackers.add(eventtracker.getString("url"));
                            }
                        }
                    }
                }
                return ad.isValid() ? ad : null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean isValid() {
        return URLUtil.isValidUrl(clickUrl) &&
                !TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(description) &&
                URLUtil.isValidUrl(iconUrl) &&
                URLUtil.isValidUrl(imageUrl) &&
                !TextUtils.isEmpty(cta);
    }

    private PrebidNativeAd() {
    }

    private void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    private void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private void setCallToAction(String cta) {
        this.cta = cta;
    }

    private void setSponsoredBy(String sponsoredBy) {
        this.sponsoredBy = sponsoredBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCallToAction() {
        return cta;
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }

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
            listener.onAdClicked();
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
