package org.prebid.mobile.configuration;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.interstitial.InterstitialSizes;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.ExoPlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AdUnitConfiguration {

    public static final String TAG = "AdUnitConfiguration";
    public static final int SKIP_OFFSET_NOT_ASSIGNED = -1;

    private boolean isRewarded;
    private boolean isBuiltInVideo = false;
    private boolean isMuted = false;
    private boolean isSoundButtonVisible = false;
    private boolean isOriginalAdUnit = false;

    private int videoSkipOffset = SKIP_OFFSET_NOT_ASSIGNED;
    private int autoRefreshDelayInMillis = 0;
    private int skipDelay = 10;
    private final int broadcastId = Utils.generateRandomInt();
    private float videoInitialVolume = ExoPlayerView.DEFAULT_INITIAL_VIDEO_VOLUME;
    private double closeButtonArea = 0;
    private double skipButtonArea = 0;

    private int maxVideoDuration = 3600;

    private String configId;
    private String pbAdSlot;
    private String interstitialSize;
    private String impressionUrl;

    private Position closeButtonPosition = Position.TOP_RIGHT;
    private Position skipButtonPosition = Position.TOP_RIGHT;
    private AdSize minSizePercentage;
    private PlacementType placementType;
    private AdPosition adPosition;
    private ContentObject appContent;
    private BannerBaseAdUnit.Parameters bannerParameters;
    private VideoBaseAdUnit.Parameters videoParameters;
    private NativeAdUnitConfiguration nativeConfiguration;

    private final EnumSet<AdFormat> adFormats = EnumSet.noneOf(AdFormat.class);
    private final HashSet<AdSize> adSizes = new HashSet<>();
    private final ArrayList<DataObject> userDataObjects = new ArrayList<>();
    private final Map<String, Set<String>> extDataDictionary = new HashMap<>();
    private final Set<String> extKeywordsSet = new HashSet<>();


    public void modifyUsingBidResponse(@Nullable BidResponse bidResponse) {
        if (bidResponse != null) {
            impressionUrl = bidResponse.getImpressionEventUrl();
        }
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setAppContent(ContentObject content) {
        appContent = content;
    }

    public ContentObject getAppContent() {
        return appContent;
    }

    public void setPbAdSlot(String pbAdSlot) {
        this.pbAdSlot = pbAdSlot;
    }

    public String getPbAdSlot() {
        return pbAdSlot;
    }

    public void addUserData(DataObject dataObject) {
        if (dataObject != null) {
            userDataObjects.add(dataObject);
        }
    }

    @NonNull
    public ArrayList<DataObject> getUserData() {
        return userDataObjects;
    }

    public void clearUserData() {
        userDataObjects.clear();
    }

    public void addExtData(
        String key,
        String value
    ) {
        if (key == null || value == null) {
            return;
        }

        if (extDataDictionary.containsKey(key)) {
            Set<String> existingSet = extDataDictionary.get(key);
            if (existingSet != null) {
                existingSet.add(value);
            }
        } else {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(value);
            extDataDictionary.put(key, hashSet);
        }
    }

    public void addExtData(
        String key,
        Set<String> value
    ) {
        if (key != null && value != null) {
            extDataDictionary.put(key, value);
        }
    }

    public void removeExtData(String key) {
        extDataDictionary.remove(key);
    }

    @NonNull
    public Map<String, Set<String>> getExtDataDictionary() {
        return extDataDictionary;
    }

    public void clearExtData() {
        extDataDictionary.clear();
    }

    public void addExtKeyword(String keyword) {
        if (keyword != null) {
            extKeywordsSet.add(keyword);
        }
    }

    public void addExtKeywords(Set<String> keywords) {
        if (keywords != null) {
            extKeywordsSet.addAll(keywords);
        }
    }

    public void removeExtKeyword(String key) {
        if (key != null) {
            extKeywordsSet.remove(key);
        }
    }

    @NonNull
    public Set<String> getExtKeywordsSet() {
        return extKeywordsSet;
    }

    public void clearExtKeywords() {
        extKeywordsSet.clear();
    }

    public void setMinSizePercentage(@Nullable AdSize minSizePercentage) {
        this.minSizePercentage = minSizePercentage;
    }

    @Nullable
    public AdSize getMinSizePercentage() {
        return minSizePercentage;
    }

    public void addSize(@Nullable AdSize size) {
        if (size != null) {
            adSizes.add(size);
        }
    }

    public void addSizes(AdSize... sizes) {
        adSizes.addAll(Arrays.asList(sizes));
    }

    public void addSizes(@Nullable Set<AdSize> sizes) {
        if (sizes != null) {
            adSizes.addAll(sizes);
        }
    }

    @NonNull
    public HashSet<AdSize> getSizes() {
        return adSizes;
    }

    public void setBannerParameters(BannerBaseAdUnit.Parameters parameters) {
        bannerParameters = parameters;
    }

    public BannerBaseAdUnit.Parameters getBannerParameters() {
        return bannerParameters;
    }

    public void setVideoParameters(VideoBaseAdUnit.Parameters parameters) {
        videoParameters = parameters;
    }

    public VideoBaseAdUnit.Parameters getVideoParameters() {
        return videoParameters;
    }


    public void setBuiltInVideo(boolean builtInVideo) {
        isBuiltInVideo = builtInVideo;
    }

    public boolean isBuiltInVideo() {
        return isBuiltInVideo;
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setIsSoundButtonVisible(boolean isSoundButtonVisible) {
        this.isSoundButtonVisible = isSoundButtonVisible;
    }

    public boolean isSoundButtonVisible() {
        return isSoundButtonVisible;
    }

    public void setAutoRefreshDelay(int autoRefreshDelayInSeconds) {
        if (autoRefreshDelayInSeconds < 0) {
            LogUtil.error(TAG, "Auto refresh delay can't be less then 0.");
            return;
        }
        if (autoRefreshDelayInSeconds == 0) {
            LogUtil.debug(TAG, "Only one request, without auto refresh.");
            autoRefreshDelayInMillis = 0;
            return;
        }
        autoRefreshDelayInMillis = Utils.clampAutoRefresh(autoRefreshDelayInSeconds);
    }

    public int getAutoRefreshDelay() {
        return autoRefreshDelayInMillis;
    }

    public void setVideoSkipOffset(int videoSkipOffset) {
        this.videoSkipOffset = videoSkipOffset;
    }

    public int getVideoSkipOffset() {
        return videoSkipOffset;
    }

    public void addAdFormat(@Nullable AdFormat adFormat) {
        if (adFormat == null) return;

        if (adFormat == AdFormat.NATIVE) {
            nativeConfiguration = new NativeAdUnitConfiguration();
        }

        adFormats.add(adFormat);
    }

    /**
     * Clears ad formats list and adds only one ad format.
     */
    public void setAdFormat(@Nullable AdFormat adFormat) {
        if (adFormat == null) return;

        if (adFormat == AdFormat.NATIVE) {
            nativeConfiguration = new NativeAdUnitConfiguration();
        }

        adFormats.clear();
        adFormats.add(adFormat);
    }

    /**
     * Clears previous ad formats and adds AdFormats corresponding to AdUnitFormat types.
     */
    public void setAdFormats(@Nullable EnumSet<AdUnitFormat> adUnitFormats) {
        if (adUnitFormats == null) return;

        adFormats.clear();

        if (adUnitFormats.contains(AdUnitFormat.DISPLAY)) {
            adFormats.add(AdFormat.INTERSTITIAL);
        }
        if (adUnitFormats.contains(AdUnitFormat.VIDEO)) {
            adFormats.add(AdFormat.VAST);
        }
    }

    public void setSkipDelay(int seconds) {
        this.skipDelay = seconds;
    }

    public int getSkipDelay() {
        return skipDelay;
    }

    public double getSkipButtonArea() {
        return skipButtonArea;
    }

    public void setSkipButtonArea(double skipButtonArea) {
        this.skipButtonArea = skipButtonArea;
    }

    @NonNull
    public Position getSkipButtonPosition() {
        return skipButtonPosition;
    }

    public void setSkipButtonPosition(@Nullable Position skipButtonPosition) {
        if (skipButtonPosition != null) {
            this.skipButtonPosition = skipButtonPosition;
        }
    }

    @NonNull
    public EnumSet<AdFormat> getAdFormats() {
        return adFormats;
    }

    public boolean isAdType(AdFormat type) {
        return adFormats.contains(type);
    }

    public void setRewarded(boolean rewarded) {
        isRewarded = rewarded;
    }

    public boolean isRewarded() {
        return isRewarded;
    }

    public void setCloseButtonArea(@FloatRange(from = 0, to = 1.0) double closeButtonArea) {
        this.closeButtonArea = closeButtonArea;
    }

    public double getCloseButtonArea() {
        return closeButtonArea;
    }

    public void setInterstitialSize(@Nullable InterstitialSizes.InterstitialSize size) {
        if (size != null) {
            interstitialSize = size.getSize();
        }
    }

    public void setInterstitialSize(@Nullable String size) {
        interstitialSize = size;
    }

    public void setInterstitialSize(
        int width,
        int height
    ) {
        interstitialSize = width + "x" + height;
    }

    @Nullable
    public String getInterstitialSize() {
        return interstitialSize;
    }

    public void setCloseButtonPosition(@Nullable Position closeButtonPosition) {
        if (closeButtonPosition != null) {
            this.closeButtonPosition = closeButtonPosition;
        }
    }

    @NonNull
    public Position getCloseButtonPosition() {
        return closeButtonPosition;
    }

    public void setVideoInitialVolume(float videoInitialVolume) {
        this.videoInitialVolume = videoInitialVolume;
    }

    public float getVideoInitialVolume() {
        return videoInitialVolume;
    }

    public void setPlacementType(@Nullable PlacementType placementType) {
        this.placementType = placementType;
    }

    public int getPlacementTypeValue() {
        return placementType != null
            ? placementType.getValue()
            : PlacementType.UNDEFINED.getValue();
    }

    public boolean isPlacementTypeValid() {
        return getPlacementTypeValue() != PlacementType.UNDEFINED.getValue();
    }

    public void setAdPosition(@Nullable AdPosition adPosition) {
        this.adPosition = adPosition;
    }

    public int getAdPositionValue() {
        return adPosition != null ? adPosition.getValue() : AdPosition.UNDEFINED.getValue();
    }

    public boolean isAdPositionValid() {
        return AdPosition.UNDEFINED.getValue() != getAdPositionValue();
    }

    public int getBroadcastId() {
        return broadcastId;
    }

    public void setMaxVideoDuration(int seconds) {
        this.maxVideoDuration = seconds;
    }

    @Nullable
    public Integer getMaxVideoDuration() {
        return maxVideoDuration;
    }

    @Nullable
    public NativeAdUnitConfiguration getNativeConfiguration() {
        return nativeConfiguration;
    }

    public boolean isOriginalAdUnit() {
        return isOriginalAdUnit;
    }

    public void setIsOriginalAdUnit(boolean originalAdUnit) {
        isOriginalAdUnit = originalAdUnit;
    }

    public String getImpressionUrl() {
        return impressionUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdUnitConfiguration that = (AdUnitConfiguration) o;

        return configId != null ? configId.equals(that.configId) : that.configId == null;
    }

    @Override
    public int hashCode() {
        return configId != null ? configId.hashCode() : 0;
    }

}
