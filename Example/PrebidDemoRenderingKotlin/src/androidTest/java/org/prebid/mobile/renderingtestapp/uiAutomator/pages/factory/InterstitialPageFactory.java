package org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory;

import androidx.test.uiautomator.UiDevice;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.PageFactory;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam.GamInterstitialPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.mopub.MoPubBiddingInterstitialPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmInterstitialPage;

public class InterstitialPageFactory extends PageFactory {

    public InterstitialPageFactory(UiDevice device) {
        super(device);
    }

    public PpmInterstitialPage goToPpmInterstitialExample(String example) {
        findListItem(example);
        return new PpmInterstitialPage(device);
    }

    public GamInterstitialPage goToGamInterstitialExample(String example) {
        findListItem(example);
        return new GamInterstitialPage(device);
    }

    public MoPubBiddingInterstitialPage goToBiddingMoPubInterstitialExample(String example) {
        findListItem(example);
        return new MoPubBiddingInterstitialPage(device);
    }
}
