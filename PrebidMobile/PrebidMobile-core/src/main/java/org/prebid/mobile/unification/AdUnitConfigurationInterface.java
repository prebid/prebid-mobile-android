package org.prebid.mobile.unification;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;

import java.util.HashSet;

public interface AdUnitConfigurationInterface extends BaseAdUnitConfigurationInterface {

    public void setMinSizePercentage(AdSize minSizePercentage);

    public AdSize getMinSizePercentage();


    public void addSize(AdSize additionalSize);

    public HashSet<AdSize> getSizes();


    public void setBannerParameters(BannerBaseAdUnit.Parameters parameters);

    public BannerBaseAdUnit.Parameters getBannerParameters();


    public void setVideoParameters(VideoBaseAdUnit.Parameters parameters);

    public VideoBaseAdUnit.Parameters getVideoParameters();

}
