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

package org.prebid.mobile.rendering.models;

import androidx.annotation.Nullable;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.interstitial.InterstitialSizes;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.video.ExoPlayerView;

import java.util.*;

public class AdConfiguration {

    public static final int SKIP_OFFSET_NOT_ASSIGNED = -1;
    private static final String TAG = "AdConfiguration";

    private final Map<String, Set<String>> mContextDataDictionary = new HashMap<>();
    private final Set<String> mContextKeywordsSet = new HashSet<>();
    private final Set<AdSize> mAdSizes = new HashSet<>();
    private final ArrayList<DataObject> userDataObjects = new ArrayList<>();

    private AdUnitIdentifierType mAdUnitIdentifierType;
    @Nullable
    private AdSize mMinSizePercentage;
    @Nullable
    private PlacementType mPlacementType;
    @Nullable
    private AdPosition mAdPosition;
    @Nullable
    private ContentObject contentObject;


    private String mInterstitialSize;
    private String mConfigId;
    @Nullable
    private String mPbAdSlot;

    private boolean mIsRewarded;
    private boolean mIsBuiltInVideo = false;

    private int mVideoSkipOffset = SKIP_OFFSET_NOT_ASSIGNED;
    private int mAutoRefreshDelayInMillis = PrebidRenderingSettings.AUTO_REFRESH_DELAY_DEFAULT;
    private final int mBroadcastId = Utils.generateRandomInt();

    private float mVideoInitialVolume = ExoPlayerView.DEFAULT_INITIAL_VIDEO_VOLUME;

    public boolean isBuiltInVideo() {
        return mIsBuiltInVideo;
    }

    public void setBuiltInVideo(boolean builtInVideo) {
        mIsBuiltInVideo = builtInVideo;
    }

    public void setAutoRefreshDelay(int autoRefreshDelay) {
        if (autoRefreshDelay < 0) {
            LogUtil.error(TAG, "Auto refresh delay can't be less then 0.");
            return;
        }
        if (autoRefreshDelay == 0) {
            LogUtil.debug(TAG, "Only one request, without auto refresh.");
            mAutoRefreshDelayInMillis = 0;
            return;
        }
        mAutoRefreshDelayInMillis = Utils.clampAutoRefresh(autoRefreshDelay);
    }

    public int getVideoSkipOffset() {
        return mVideoSkipOffset;
    }

    public void setVideoSkipOffset(int videoSkipOffset) {
        mVideoSkipOffset = videoSkipOffset;
    }

    public int getAutoRefreshDelay() {
        return mAutoRefreshDelayInMillis;
    }

    public AdUnitIdentifierType getAdUnitIdentifierType() {
        return mAdUnitIdentifierType;
    }

    public void setAdUnitIdentifierType(AdUnitIdentifierType adUnitIdentifierType) {
        mAdUnitIdentifierType = adUnitIdentifierType;
    }

    public boolean isRewarded() {
        return mIsRewarded;
    }

    public void setRewarded(boolean rewarded) {
        mIsRewarded = rewarded;
    }

    public String getInterstitialSize() {
        return mInterstitialSize;
    }

    public void setInterstitialSize(InterstitialSizes.InterstitialSize size) {
        mInterstitialSize = size.getSize();
    }

    public void setInterstitialSize(String size) {
        mInterstitialSize = size;
    }

    public void setInterstitialSize(int width, int height) {
        mInterstitialSize = width + "x" + height;
    }

    public void setVideoInitialVolume(float videoInitialVolume) {
        mVideoInitialVolume = videoInitialVolume;
    }

    public float getVideoInitialVolume() {
        return mVideoInitialVolume;
    }

    public void setAppContent(@Nullable ContentObject contentObject) {
        this.contentObject = contentObject;
    }

    @Nullable
    public ContentObject getAppContent() {
        return contentObject;
    }

    public boolean isAdType(AdUnitIdentifierType type) {
        return mAdUnitIdentifierType == type;
    }

    public void addSize(AdSize size) {
        if (size != null) {
            mAdSizes.add(size);
        }
    }

    public void addSizes(AdSize... sizes) {
        mAdSizes.addAll(Arrays.asList(sizes));
    }

    public Set<AdSize> getAdSizes() {
        return mAdSizes;
    }

    public void addContextData(String key, String value) {
        addValue(mContextDataDictionary, key, value);
    }

    public void updateContextData(String key, Set<String> value) {
        mContextDataDictionary.put(key, value);
    }

    public void removeContextData(String key) {
        mContextDataDictionary.remove(key);
    }

    public void clearContextData() {
        mContextDataDictionary.clear();
    }

    public Map<String, Set<String>> getContextDataDictionary() {
        return new HashMap<>(mContextDataDictionary);
    }

    public void addContextKeyword(String keyword) {
        mContextKeywordsSet.add(keyword);
    }

    public void addContextKeywords(Set<String> keywords) {
        mContextKeywordsSet.addAll(keywords);
    }

    public void removeContextKeyword(String keyword) {
        mContextKeywordsSet.remove(keyword);
    }

    public Set<String> getContextKeywordsSet() {
        return new HashSet<>(mContextKeywordsSet);
    }

    public void clearContextKeywords() {
        mContextKeywordsSet.clear();
    }

    private <E, U> void addValue(Map<E, Set<U>> map, E key, U value) {
        Set<U> valueSet = map.get(key);

        if (valueSet == null) {
            valueSet = new HashSet<>();
            map.put(key, valueSet);
        }

        valueSet.add(value);
    }

    public String getConfigId() {
        return mConfigId;
    }

    public void setConfigId(String configId) {
        mConfigId = configId;
    }

    public void setPbAdSlot(
        @Nullable
            String pbAdSlot) {
        mPbAdSlot = pbAdSlot;
    }

    @Nullable
    public String getPbAdSlot() {
        return mPbAdSlot;
    }

    @Nullable
    public AdSize getMinSizePercentage() {
        return mMinSizePercentage;
    }

    public void setMinSizePercentage(
        @Nullable
            AdSize minSizePercentage) {
        mMinSizePercentage = minSizePercentage;
    }

    public int getPlacementTypeValue() {
        return mPlacementType != null
               ? mPlacementType.getValue()
               : PlacementType.UNDEFINED.getValue();
    }

    public void setPlacementType(
        @Nullable
            PlacementType placementType) {
        mPlacementType = placementType;
    }

    public void setAdPosition(
        @Nullable
            AdPosition adPosition) {
        mAdPosition = adPosition;
    }

    public int getAdPositionValue() {
        return mAdPosition != null ? mAdPosition.getValue() : AdPosition.UNDEFINED.getValue();
    }

    public boolean isAdPositionValid() {
        return AdPosition.UNDEFINED.getValue() != getAdPositionValue();
    }

    public int getBroadcastId() {
        return mBroadcastId;
    }

    public boolean isPlacementTypeValid() {
        return getPlacementTypeValue() != PlacementType.UNDEFINED.getValue();
    }

    public ArrayList<DataObject> getUserData() {
        return userDataObjects;
    }

    public void addUserData(DataObject dataObject) {
        userDataObjects.add(dataObject);
    }

    public void clearUserData() {
        userDataObjects.clear();
    }

    public enum AdUnitIdentifierType {
        BANNER,
        INTERSTITIAL,
        NATIVE, VAST
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdConfiguration that = (AdConfiguration) o;

        return mConfigId != null ? mConfigId.equals(that.mConfigId) : that.mConfigId == null;
    }

    @Override
    public int hashCode() {
        return mConfigId != null ? mConfigId.hashCode() : 0;
    }
}
