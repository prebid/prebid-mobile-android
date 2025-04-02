package org.prebid.mobile.configuration;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.interstitial.InterstitialSizes;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardManager;
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
    private boolean hasEndCard = false;

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
    private String fingerprint = Utils.generateUUIDTimeBased();
    @Nullable
    private String gpid;
    @Nullable
    private String impOrtbConfig;

    private Position closeButtonPosition = Position.TOP_RIGHT;
    private Position skipButtonPosition = Position.TOP_RIGHT;
    private AdSize minSizePercentage;
    private PlacementType placementType;
    @NonNull
    private AdPosition adPosition = AdPosition.UNDEFINED;
    @Nullable
    private ContentObject appContent;
    private BannerParameters bannerParameters;
    private VideoParameters videoParameters;
    private NativeAdUnitConfiguration nativeConfiguration;
    private RewardManager rewardManager = new RewardManager();

    private final EnumSet<AdFormat> adFormats = EnumSet.noneOf(AdFormat.class);
    private final HashSet<AdSize> adSizes = new HashSet<>();

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

    public void setPbAdSlot(String pbAdSlot) {
        this.pbAdSlot = pbAdSlot;
    }

    public String getPbAdSlot() {
        return pbAdSlot;
    }

    public void setMinSizePercentage(@Nullable AdSize minSizePercentage) {
        this.minSizePercentage = minSizePercentage;
    }

    @Nullable
    public AdSize getMinSizePercentage() {
        return minSizePercentage;
    }

    /**
     * Should be replaced by the sizes in {@link BannerParameters} or {@link VideoParameters}.
     */
    @Deprecated
    public void addSize(@Nullable AdSize size) {
        if (size != null) {
            adSizes.add(size);
        }
    }

    /**
     * Should be replaced by the sizes in {@link BannerParameters} or {@link VideoParameters}.
     */
    @Deprecated
    public void addSizes(AdSize... sizes) {
        adSizes.addAll(Arrays.asList(sizes));
    }

    /**
     * Should be replaced by the sizes in {@link BannerParameters} or {@link VideoParameters}.
     */
    @Deprecated
    public void addSizes(@Nullable Set<AdSize> sizes) {
        if (sizes != null) {
            adSizes.addAll(sizes);
        }
    }

    /**
     * Should be replaced by the sizes in {@link BannerParameters} or {@link VideoParameters}.
     */
    @Deprecated
    @NonNull
    public HashSet<AdSize> getSizes() {
        return adSizes;
    }

    public void setBannerParameters(BannerParameters parameters) {
        bannerParameters = parameters;
    }

    public BannerParameters getBannerParameters() {
        return bannerParameters;
    }

    public void setVideoParameters(VideoParameters parameters) {
        videoParameters = parameters;
    }

    public VideoParameters getVideoParameters() {
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
    public void setAdUnitFormats(@Nullable EnumSet<AdUnitFormat> adUnitFormats) {
        if (adUnitFormats == null) return;

        adFormats.clear();
        adFormats.addAll(AdFormat.fromSet(adUnitFormats, true));
    }

    /**
     * Clears previous ad formats and adds AdFormats corresponding to AdUnitFormat types.
     */
    public void setAdFormats(@Nullable EnumSet<AdFormat> formats) {
        if (formats == null) return;

        if (formats.contains(AdFormat.NATIVE)) {
            nativeConfiguration = new NativeAdUnitConfiguration();
        }

        adFormats.clear();
        adFormats.addAll(formats);
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

    public void setAdPosition(AdPosition adPosition) {
        if (adPosition == null) return;

        this.adPosition = adPosition;
    }

    @NonNull
    public AdPosition getAdPosition() {
        return adPosition;
    }

    public int getAdPositionValue() {
        return adPosition.getValue();
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

    public void setNativeConfiguration(NativeAdUnitConfiguration nativeConfiguration) {
        this.nativeConfiguration = nativeConfiguration;
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

    public String getFingerprint() {
        return fingerprint;
    }

    @Nullable
    public String getGpid() {
        return gpid;
    }

    public void setGpid(@Nullable String gpid) {
        this.gpid = gpid;
    }

    @Nullable
    public String getImpOrtbConfig() {
        return impOrtbConfig;
    }

    public void setImpOrtbConfig(@Nullable String impOrtbConfig) {
        this.impOrtbConfig = impOrtbConfig;
    }

    public boolean getHasEndCard() {
        return hasEndCard;
    }

    public void setHasEndCard(boolean hasEndCard) {
        this.hasEndCard = hasEndCard;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public void setRewardManager(RewardManager rewardManager) {
        this.rewardManager = rewardManager;
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
