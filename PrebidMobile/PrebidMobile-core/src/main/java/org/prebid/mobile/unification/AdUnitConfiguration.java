package org.prebid.mobile.unification;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;

import java.util.HashSet;

public class AdUnitConfiguration extends BaseAdUnitConfiguration implements AdUnitConfigurationInterface {

    private AdSize minSizePercentage;
    private HashSet<AdSize> sizes;
    private BannerBaseAdUnit.Parameters bannerParameters;
    private VideoBaseAdUnit.Parameters videoParameters;

    @Override
    public void setMinSizePercentage(AdSize minSizePercentage) {
        this.minSizePercentage = minSizePercentage;
    }

    @Override
    public AdSize getMinSizePercentage() {
        return minSizePercentage;
    }

    @Override
    public void addSize(AdSize additionalSize) {
        sizes.add(additionalSize);
    }

    @Override
    public HashSet<AdSize> getSizes() {
        return sizes;
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

}
