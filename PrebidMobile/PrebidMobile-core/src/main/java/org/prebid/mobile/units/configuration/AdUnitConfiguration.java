package org.prebid.mobile.units.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.rendering.interstitial.InterstitialSizes;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.video.ExoPlayerView;
import org.prebid.mobile.unification.AdUnitConfigurationInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.prebid.mobile.PrebidMobile.AUTO_REFRESH_DELAY_DEFAULT;

public class AdUnitConfiguration extends BaseAdUnitConfiguration implements AdUnitConfigurationInterface {

    public static final String TAG = "AdUnitConfiguration";
    public static final int SKIP_OFFSET_NOT_ASSIGNED = -1;

    private boolean isRewarded;
    private boolean isBuiltInVideo = false;

    private int videoSkipOffset = SKIP_OFFSET_NOT_ASSIGNED;
    private int autoRefreshDelayInMillis = AUTO_REFRESH_DELAY_DEFAULT;
    private final int broadcastId = Utils.generateRandomInt();
    private float videoInitialVolume = ExoPlayerView.DEFAULT_INITIAL_VIDEO_VOLUME;

    private String interstitialSize;

    private AdUnitIdentifierType adUnitIdentifierType;
    private AdSize minSizePercentage;
    private PlacementType placementType;
    private AdPosition adPosition;
    private BannerBaseAdUnit.Parameters bannerParameters;
    private VideoBaseAdUnit.Parameters videoParameters;

    private final HashSet<AdSize> adSizes = new HashSet<>();

    @Override
    public void setMinSizePercentage(@Nullable AdSize minSizePercentage) {
        this.minSizePercentage = minSizePercentage;
    }

    @Override
    @Nullable
    public AdSize getMinSizePercentage() {
        return minSizePercentage;
    }

    @Override
    public void addSize(@Nullable AdSize size) {
        if (size != null) {
            adSizes.add(size);
        }
    }

    public void addSizes(AdSize... sizes) {
        adSizes.addAll(Arrays.asList(sizes));
    }

    @Override
    public void addSizes(@Nullable Set<AdSize> sizes) {
        if (sizes != null) {
            adSizes.addAll(sizes);
        }
    }

    @Override
    @NonNull
    public HashSet<AdSize> getSizes() {
        return adSizes;
    }

    @Override
    public void setBannerParameters(BannerBaseAdUnit.Parameters parameters) {
        bannerParameters = parameters;
    }

    @Override
    public BannerBaseAdUnit.Parameters getBannerParameters() {
        return bannerParameters;
    }

    @Override
    public void setVideoParameters(VideoBaseAdUnit.Parameters parameters) {
        videoParameters = parameters;
    }

    @Override
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
            LogUtil.error(TAG, "Auto refresh delay can't be less then 0.");
            return;
        }
        if (autoRefreshDelay == 0) {
            LogUtil.debug(TAG, "Only one request, without auto refresh.");
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
