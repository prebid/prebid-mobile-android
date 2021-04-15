/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.bidding.data.ntv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetImage;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.ArrayList;
import java.util.List;

public class NativeAdParser {
    private static final String TAG = NativeAdParser.class.getSimpleName();

    @Nullable
    public NativeAd parse(String adm) {

        try {
            JSONObject admJson = new JSONObject(adm);
            List<NativeAdEventTracker> nativeAdEventTrackerList = new ArrayList<>();

            JSONArray assetArray = admJson.optJSONArray("assets");
            JSONArray eventTrackersArray = admJson.optJSONArray("eventtrackers");
            JSONObject linkJson = admJson.optJSONObject("link");
            String version = admJson.optString("ver");
            Ext ext = parseExt(admJson);

            if (assetArray == null) {
                OXLog.error(TAG, "parse: Failed. Asset array is null. Returning null.");
                return null;
            }

            List<NativeAdTitle> nativeAdTitleList = new ArrayList<>();
            List<NativeAdImage> nativeAdImageList = new ArrayList<>();
            List<NativeAdVideo> nativeAdVideoList = new ArrayList<>();
            List<NativeAdData> nativeAdDataList = new ArrayList<>();

            for (int i = 0; i < assetArray.length(); i++) {
                JSONObject asset = assetArray.optJSONObject(i);
                if (asset == null) {
                    OXLog.debug(TAG, "parse: Skipping asset parse at index: " + i + ". Reason: asset is null");
                    continue;
                }

                NativeAdTitle title = parseTitle(asset);
                NativeAdImage image = parseImage(asset);
                NativeAdVideo video = parseVideo(asset);
                NativeAdData data = parseData(asset);

                setAssetLink(title, asset);
                setAssetLink(image, asset);
                setAssetLink(video, asset);
                setAssetLink(data, asset);

                insertIfNonNull(nativeAdTitleList, title);
                insertIfNonNull(nativeAdImageList, image);
                insertIfNonNull(nativeAdVideoList, video);
                insertIfNonNull(nativeAdDataList, data);
            }

            NativeAdLink nativeAdLink = parseNativeAdLink(linkJson);
            parseEventTrackerArray(nativeAdEventTrackerList, eventTrackersArray);

            return new NativeAd(version,
                                nativeAdLink,
                                ext,
                                nativeAdTitleList,
                                nativeAdImageList,
                                nativeAdDataList,
                                nativeAdVideoList,
                                nativeAdEventTrackerList);
        }
        catch (JSONException e) {
            OXLog.error(TAG, "parse: Failed. Returning null. Details: " + e);
        }

        return null;
    }

    private NativeAdTitle parseTitle(
        @NonNull
            JSONObject assetJson) {
        JSONObject titleJson = assetJson.optJSONObject("title");
        if (titleJson == null) {
            return null;
        }

        String text = titleJson.optString("text");
        Integer len = optInteger(titleJson, "len");
        Ext ext = parseExt(titleJson);

        return new NativeAdTitle(text, len, ext);
    }

    private NativeAdImage parseImage(
        @NonNull
            JSONObject assetJson) {
        JSONObject imageJson = assetJson.optJSONObject("img");
        if (imageJson == null) {
            return null;
        }

        Integer imageTypeInt = optInteger(imageJson, "type");
        NativeAssetImage.ImageType imageType = NativeAssetImage.ImageType.getType(imageTypeInt);
        String url = imageJson.optString("url");
        Integer w = optInteger(imageJson, "w");
        Integer h = optInteger(imageJson, "h");
        Ext ext = parseExt(imageJson);

        return new NativeAdImage(imageType, url, w, h, ext);
    }

    private NativeAdVideo parseVideo(
        @NonNull
            JSONObject assetJson) {
        JSONObject videoJson = assetJson.optJSONObject("video");
        if (videoJson == null) {
            return null;
        }

        String vastTag = videoJson.optString("vasttag");
        MediaData mediaData = new MediaData(vastTag);

        return new NativeAdVideo(mediaData);
    }

    private NativeAdData parseData(
        @NonNull
            JSONObject assetJson) {
        JSONObject dataJson = assetJson.optJSONObject("data");
        if (dataJson == null) {
            return null;
        }

        Integer dataTypeInt = optInteger(dataJson, "type");
        NativeAssetData.DataType dataType = NativeAssetData.DataType.getType(dataTypeInt);
        String value = dataJson.optString("value");
        Integer len = optInteger(dataJson, "len");
        Ext ext = parseExt(dataJson);

        return new NativeAdData(dataType, value, len, ext);
    }

    private NativeAdLink parseNativeAdLink(JSONObject linkJson) {
        if (linkJson == null) {
            return null;
        }

        String url = linkJson.optString("url");
        JSONArray clickTrackersJsonArray = linkJson.optJSONArray("clicktrackers");
        String fallback = linkJson.optString("fallback");
        Ext ext = parseExt(linkJson);

        List<String> clickTrackersList = new ArrayList<>();

        if (clickTrackersJsonArray != null && clickTrackersJsonArray.length() > 0) {
            for (int i = 0; i < clickTrackersJsonArray.length(); i++) {
                clickTrackersList.add(clickTrackersJsonArray.optString(i));
            }
        }

        return new NativeAdLink(url, clickTrackersList, fallback, ext);
    }

    private void parseEventTrackerArray(List<NativeAdEventTracker> nativeAdEventTrackerList, JSONArray eventTrackersArray) {
        if (eventTrackersArray == null) {
            return;
        }

        for (int i = 0; i < eventTrackersArray.length(); i++) {
            NativeAdEventTracker eventTracker = parseNativeAdEventTracker(eventTrackersArray.optJSONObject(i));
            insertIfNonNull(nativeAdEventTrackerList, eventTracker);
        }
    }

    private NativeAdEventTracker parseNativeAdEventTracker(JSONObject eventJson) {
        if (eventJson == null) {
            return null;
        }

        // Don't include eventTrackers that don't contain required fields.
        // Event trackers are used internally.
        if (!eventJson.has("event") || !eventJson.has("method")) {
            return null;
        }

        Integer eventTypeInt = optInteger(eventJson, "event");
        NativeEventTracker.EventType eventType = NativeEventTracker.EventType.getType(eventTypeInt);
        Integer eventTrackingMethodInt = optInteger(eventJson, "method");
        NativeEventTracker.EventTrackingMethod eventTrackingMethod = NativeEventTracker.EventTrackingMethod.getType(eventTrackingMethodInt);
        String url = eventJson.optString("url");
        String customData = eventJson.optString("customdata");
        Ext ext = parseExt(eventJson);

        return new NativeAdEventTracker(eventType, eventTrackingMethod, url, customData, ext);
    }

    private Ext parseExt(JSONObject assetJson) {
        JSONObject extJson = assetJson.optJSONObject("ext");

        if (extJson == null) {
            return null;
        }

        Ext ext = new Ext();
        ext.put(extJson);
        return ext;
    }

    private void setAssetLink(BaseNativeAdElement nativeAd, JSONObject assetJson) {
        if (nativeAd == null || assetJson == null) {
            return;
        }

        NativeAdLink nativeAdLink = parseNativeAdLink(assetJson.optJSONObject("link"));
        nativeAd.setNativeAdLink(nativeAdLink);
    }

    @Nullable
    private Integer optInteger(JSONObject jsonObject, String key) {
        return ((Integer) jsonObject.opt(key));
    }

    private <T> void insertIfNonNull(
        @NonNull
            List<T> list,
        @Nullable
            T value) {
        if (value == null) {
            return;
        }

        list.add(value);
    }
}
