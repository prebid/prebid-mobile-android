package com.openx.internal_test_app.uiAutomator.pages.factory;

import androidx.test.uiautomator.UiDevice;

import com.openx.internal_test_app.uiAutomator.pages.PageFactory;
import com.openx.internal_test_app.uiAutomator.pages.bidding.gam.GamInterstitialPage;
import com.openx.internal_test_app.uiAutomator.pages.bidding.mopub.MoPubBiddingInterstitialPage;
import com.openx.internal_test_app.uiAutomator.pages.bidding.ppm.PpmInterstitialPage;

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
