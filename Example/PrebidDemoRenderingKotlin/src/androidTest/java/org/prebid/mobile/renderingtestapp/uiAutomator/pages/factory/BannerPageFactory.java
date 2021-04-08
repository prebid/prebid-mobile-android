package org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory;

import androidx.test.uiautomator.UiDevice;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.PageFactory;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam.GamBannerPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.mopub.MoPubBiddingBannerPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmBannerPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmBannerVideoPage;

public class BannerPageFactory extends PageFactory {

    public BannerPageFactory(UiDevice device) {
        super(device);
    }

    public GamBannerPage goToGamBannerExample(String example) {
        findListItem(example);
        return new GamBannerPage(device);
    }

    public PpmBannerPage goToPpmBannerExample(String example) {
        findListItem(example);
        return new PpmBannerPage(device);
    }

    public PpmBannerVideoPage goToPpmBannerVideoExample(String example) {
        findListItem(example);
        return new PpmBannerVideoPage(device);
    }

    public MoPubBiddingBannerPage goToBiddingMoPubBannerExample(String example) {
        findListItem(example);
        return new MoPubBiddingBannerPage(device);
    }
}
