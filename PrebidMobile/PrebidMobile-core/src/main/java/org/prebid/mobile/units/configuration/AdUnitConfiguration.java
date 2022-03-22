package org.prebid.mobile.units.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.*;
import org.prebid.mobile.rendering.interstitial.InterstitialSizes;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.ExoPlayerView;

import java.util.*;

import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_DEFAULT;

public class AdUnitConfiguration {

    public static final String TAG = "AdUnitConfiguration";
    public static final int SKIP_OFFSET_NOT_ASSIGNED = -1;

    private boolean isRewarded;
    private boolean isBuiltInVideo = false;

    private int videoSkipOffset = SKIP_OFFSET_NOT_ASSIGNED;
    private int autoRefreshDelayInMillis = AUTO_REFRESH_DELAY_DEFAULT;
    private final int broadcastId = Utils.generateRandomInt();
    private float videoInitialVolume = ExoPlayerView.DEFAULT_INITIAL_VIDEO_VOLUME;

    private String configId;
    private String pbAdSlot;
    private String interstitialSize;

    private AdType adType;
    private AdUnitIdentifierType adUnitIdentifierType;
    private AdSize minSizePercentage;
    private PlacementType placementType;
    private AdPosition adPosition;
    private ContentObject appContent;
    private BannerBaseAdUnit.Parameters bannerParameters;
    private VideoBaseAdUnit.Parameters videoParameters;
    private NativeAdUnitConfiguration nativeConfiguration;

    private final HashSet<AdSize> adSizes = new HashSet<>();
    private final ArrayList<DataObject> userDataObjects = new ArrayList<>();
    private final Map<String, Set<String>> contextDataDictionary = new HashMap<>();
    private final Set<String> contextKeywordsSet = new HashSet<>();


    public void setAdType(AdType adType) {
        this.adType = adType;
    }

    public AdType getAdType() {
        return adType;
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

    public void addContextData(String key, String value) {
        if (key != null && value != null) {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(value);
            contextDataDictionary.put(key, hashSet);
        }
    }

    public void addContextData(String key, Set<String> value) {
        if (key != null && value != null) {
            contextDataDictionary.put(key, value);
        }
    }

    public void removeContextData(String key) {
        contextDataDictionary.remove(key);
    }

    @NonNull
    public Map<String, Set<String>> getContextDataDictionary() {
        return contextDataDictionary;
    }

    public void clearContextData() {
        contextDataDictionary.clear();
    }

    public void addContextKeyword(String keyword) {
        if (keyword != null) {
            contextKeywordsSet.add(keyword);
        }
    }

    public void addContextKeywords(Set<String> keywords) {
        if (keywords != null) {
            contextKeywordsSet.addAll(keywords);
        }
    }

    public void removeContextKeyword(String key) {
        if (key != null) {
            contextKeywordsSet.remove(key);
        }
    }

    @NonNull
    public Set<String> getContextKeywordsSet() {
        return contextKeywordsSet;
    }

    public void clearContextKeywords() {
        contextKeywordsSet.clear();
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

    public void setAutoRefreshDelay(int autoRefreshDelay) {
        if (autoRefreshDelay < 0) {
            LogUtil.e(TAG, "Auto refresh delay can't be less then 0.");
            return;
        }
        if (autoRefreshDelay == 0) {
            LogUtil.d(TAG, "Only one request, without auto refresh.");
            autoRefreshDelayInMillis = 0;
            return;
        }
        autoRefreshDelayInMillis = Utils.clampAutoRefresh(autoRefreshDelay);
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

    public void setAdUnitIdentifierType(@Nullable AdUnitIdentifierType adUnitIdentifierType) {
        this.adUnitIdentifierType = adUnitIdentifierType;
    }

    @Nullable
    public AdUnitIdentifierType getAdUnitIdentifierType() {
        return adUnitIdentifierType;
    }

    public boolean isAdType(AdUnitIdentifierType type) {
        return adUnitIdentifierType == type;
    }

    public void setRewarded(boolean rewarded) {
        isRewarded = rewarded;
    }

    public boolean isRewarded() {
        return isRewarded;
    }

    public void setInterstitialSize(@Nullable InterstitialSizes.InterstitialSize size) {
        if (size != null) {
            interstitialSize = size.getSize();
        }
    }

    public void setInterstitialSize(@Nullable String size) {
        interstitialSize = size;
    }

    public void setInterstitialSize(int width, int height) {
        interstitialSize = width + "x" + height;
    }

    @Nullable
    public String getInterstitialSize() {
        return interstitialSize;
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


    public boolean isNative() {
        return nativeConfiguration != null;
    }

    /**
     * Creates native configuration.
     */
    public void initNativeConfiguration() {
        nativeConfiguration = new NativeAdUnitConfiguration();
    }

    @Nullable
    public NativeAdUnitConfiguration getNativeConfiguration() {
        return nativeConfiguration;
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


    public enum AdUnitIdentifierType {
        BANNER,
        INTERSTITIAL,
        NATIVE,
        VAST
    }

}
