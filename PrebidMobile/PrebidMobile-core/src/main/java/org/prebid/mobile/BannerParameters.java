package org.prebid.mobile;


import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Banner parameters for the
 * <a href="https://www.iab.com/wp-content/uploads/2016/03/OpenRTB-API-Specification-Version-2-5-FINAL.pdf">OpenRTB</a>
 * banner object.
 */
public class BannerParameters {

    /**
     * List of supported API frameworks for this impression. If an API is not explicitly listed, it is assumed not to be supported.
     */
    @Nullable
    private List<Signals.Api> api;
    @Nullable
    private Set<AdSize> adSizes;
    @Nullable
    private Integer interstitialMinWidthPercentage;
    @Nullable
    private Integer interstitialMinHeightPercentage;

    @Nullable
    public List<Signals.Api> getApi() {
        return api;
    }

    public void setApi(@Nullable List<Signals.Api> api) {
        this.api = api;
    }

    @Nullable
    public Set<AdSize> getAdSizes() {
        return adSizes;
    }

    public void setAdSizes(@Nullable Set<AdSize> adSizes) {
        if (adSizes == null) {
            this.adSizes = null;
            return;
        }
        this.adSizes = new HashSet<>(adSizes);
    }

    @Nullable
    public Integer getInterstitialMinWidthPercentage() {
        return interstitialMinWidthPercentage;
    }

    public void setInterstitialMinWidthPercentage(@Nullable Integer interstitialMinWidthPercentage) {
        this.interstitialMinWidthPercentage = interstitialMinWidthPercentage;
    }

    @Nullable
    public Integer getInterstitialMinHeightPercentage() {
        return interstitialMinHeightPercentage;
    }

    public void setInterstitialMinHeightPercentage(@Nullable Integer interstitialMinHeightPercentage) {
        this.interstitialMinHeightPercentage = interstitialMinHeightPercentage;
    }

}
