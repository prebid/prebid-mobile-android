package org.prebid.mobile.unification;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;

import java.util.HashSet;
import java.util.Set;

public interface AdUnitConfigurationInterface extends BaseAdUnitConfigurationInterface {

    public void setMinSizePercentage(AdSize minSizePercentage);

    public AdSize getMinSizePercentage();


    public void addSize(AdSize size);

    public void addSizes(Set<AdSize> sizes);

    public HashSet<AdSize> getSizes();


    public void setBannerParameters(BannerBaseAdUnit.Parameters parameters);

    public BannerBaseAdUnit.Parameters getBannerParameters();


    public void setVideoParameters(VideoBaseAdUnit.Parameters parameters);

    public VideoBaseAdUnit.Parameters getVideoParameters();

}
