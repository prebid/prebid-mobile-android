package com.mopub.mediation;

import androidx.annotation.Nullable;
import com.mopub.mobileads.MoPubInterstitial;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class MoPubInterstitialMediationUtils extends MoPubBaseMediationUtils {

    private static final String TAG = "MoPubInterstitial";
    private final WeakReference<MoPubInterstitial> adViewReference;

    public MoPubInterstitialMediationUtils(MoPubInterstitial adObject) {
        adViewReference = new WeakReference<>(adObject);
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        setResponseToLocalExtras(response, localExtras -> {
            MoPubInterstitial adView = adViewReference.get();
            if (isAdViewNull(adView)) {
                return;
            }
            adView.setLocalExtras(localExtras);
        });
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {
        handleKeywordsUpdate(keywords, new KeywordsManager() {
            @Override
            public void setKeywords(String keywords) {
                MoPubInterstitial adView = adViewReference.get();
                if (isAdViewNull(adView)) {
                    return;
                }
                adView.setKeywords(keywords);
            }

            @Override
            public String getKeywords() {
                MoPubInterstitial adView = adViewReference.get();
                if (isAdViewNull(adView)) {
                    return "";
                }
                return adView.getKeywords();
            }
        });
    }
}
