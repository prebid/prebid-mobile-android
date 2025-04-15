package org.prebid.mobile.api.original;

import org.jetbrains.annotations.Nullable;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.NativeParameters;
import org.prebid.mobile.VideoParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Request parameters for {@link PrebidAdUnit}.
 */
public class PrebidRequest {

    @Nullable
    private BannerParameters bannerParameters;
    @Nullable
    private VideoParameters videoParameters;
    @Nullable
    private NativeParameters nativeParameters;

    private boolean isInterstitial = false;
    private boolean isRewarded = false;

    @Nullable
    private String gpid;

    public PrebidRequest() {
    }


    BannerParameters getBannerParameters() {
        return bannerParameters;
    }

    public void setBannerParameters(BannerParameters bannerParameters) {
        this.bannerParameters = bannerParameters;
    }

    VideoParameters getVideoParameters() {
        return videoParameters;
    }

    public void setVideoParameters(VideoParameters videoParameters) {
        this.videoParameters = videoParameters;
    }

    NativeParameters getNativeParameters() {
        return nativeParameters;
    }

    public void setNativeParameters(NativeParameters nativeParameters) {
        this.nativeParameters = nativeParameters;
    }

    boolean isInterstitial() {
        return isInterstitial;
    }

    public void setInterstitial(boolean interstitial) {
        this.isInterstitial = interstitial;
    }

    boolean isRewarded() {
        return isRewarded;
    }

    public void setRewarded(boolean rewarded) {
        if (rewarded) {
            this.isInterstitial = true;
            this.isRewarded = true;
        }
    }


    @Nullable
    String getGpid() {
        return gpid;
    }

    public void setGpid(@Nullable String gpid) {
        this.gpid = gpid;
    }
}
