package org.prebid.mobile.drprebid.validation;

import android.app.Activity;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import org.prebid.mobile.drprebid.managers.LineItemKeywordManager;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.*;

import java.util.*;

import static org.prebid.mobile.drprebid.managers.LineItemKeywordManager.KEYWORD_REQUEST_ID;

public class AdServerTest {
    private static final String TAG = AdServerTest.class.getSimpleName();

    public interface Listener {

        void onPrebidKeywordsFoundOnRequest();

        void onPrebidKeywordsNotFoundOnRequest();

        void adServerResponseContainsPrebidCreative(@Nullable Boolean contains);

        void onTestFinished();

    }

    private final Listener listener;
    private final Activity context;

    private PublisherAdView googleAd;
    private PublisherInterstitialAd googleInterstitial;

    private String adServerResponse = "";
    private String requestId;
    private Map<String, String> keywords;

    public AdServerTest(
            Activity context,
            Listener listener
    ) {
        this.context = context;
        this.listener = listener;
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(context).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(context).getAdServerSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(context).getPrebidServerSettings();

        switch (generalSettings.getAdSize()) {
            case BANNER_300x250:
                break;
            case BANNER_300x600:
                break;
            case BANNER_320x50:
                break;
            case BANNER_320x100:
                break;
            case BANNER_320x480:
                break;
            case BANNER_728x90:
                break;
        }

        keywords = createMapKeywords(adServerSettings.getBidPrice(),
                generalSettings.getAdSize(),
                generalSettings.getAdFormat(),
                prebidServerSettings.getPrebidServer()
        );

        PublisherAdRequest adRequest = null;

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            googleAd = new PublisherAdView(context);
            AdSize adSize = generalSettings.getAdSize();
            googleAd.setAdSizes(new com.google.android.gms.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
            googleAd.setAdUnitId(adServerSettings.getAdUnitId());
            googleAd.setAdListener(googleBannerListener);

        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            googleInterstitial = new PublisherInterstitialAd(context);
            googleInterstitial.setAdUnitId(adServerSettings.getAdUnitId());
            googleInterstitial.setAdListener(googleInterstitialListener);
        }

        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

        for (String key : keywords.keySet()) {
            if (keywords.containsKey(key)) {
                adRequestBuilder.addCustomTargeting(key, keywords.get(key));
            }
        }

        adRequest = adRequestBuilder.build();

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            googleAd.loadAd(adRequest);
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            googleInterstitial.loadAd(adRequest);
        }

        checkRequestForKeywordsAM(adRequest);

    }

    private String createStringKeywords() {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : keywords.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(LineItemKeywordManager.KEYWORD_COMMA);
            }

            String keyword = String.format(Locale.ENGLISH, "%s:%s", key, keywords.get(key));
            stringBuilder.append(keyword);
        }

        return stringBuilder.toString();
    }

    private Map<String, String> createMapKeywords(float bidPrice, AdSize adSize, AdFormat adFormat, PrebidServer prebidServer) {
        Map<String, String> keywords = new HashMap<>(LineItemKeywordManager.getInstance().getMapKeywords(bidPrice, adSize, adFormat, prebidServer));

        requestId = UUID.randomUUID().toString();

        keywords.put(KEYWORD_REQUEST_ID, requestId);

        return keywords;
    }

    //--------------------------------- Google Banner Listener -------------------------------------

    private AdListener googleBannerListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            AdViewUtils.findHtml(googleAd, new OnWebViewListener() {
                @Override
                public void success(String html) {
                    adServerResponse = html;
                    checkResponseForPrebidCreative();
                }

                @Override
                public void failure() {
                    invokeContainsPrebidCreative(false);
                }
            });
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);

            invokeContainsPrebidCreative(false);
        }
    };

    //------------------------------- Google Interstitial Listener ---------------------------------

    private AdListener googleInterstitialListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            invokeContainsPrebidCreative(null);
            invokeTestFinished();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);

            invokeContainsPrebidCreative(false);
        }
    };



    //Check PublisherAdRequest
    private void checkRequestForKeywordsAM(@Nullable PublisherAdRequest adRequest) {
        if (adRequest != null && listener != null) {

            Map<String, String> map = new HashMap<>();

            Set<String> set = adRequest.getCustomTargeting().keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                map.put(key, adRequest.getCustomTargeting().getString(key));
            }

            if (map.get(KEYWORD_REQUEST_ID).equals(requestId)) {
                if (keywords.equals(map)) {
                    listener.onPrebidKeywordsFoundOnRequest();
                } else {
                    listener.onPrebidKeywordsNotFoundOnRequest();
                }
            }
        }
    }

    private void checkRequestForKeywords(String url, String postBody) {
        if (url.contains(requestId) || postBody.contains(requestId)) {
            boolean containsKeyValues = true;

            for (String key : keywords.keySet()) {
                String keyValuePair = String.format(Locale.ENGLISH, "%s:%s", key, keywords.get(key));
                if (!postBody.contains(keyValuePair)) {
                    containsKeyValues = false;
                }
            }

            if (listener != null) {
                if (containsKeyValues) {
                    listener.onPrebidKeywordsFoundOnRequest();
                } else {
                    listener.onPrebidKeywordsNotFoundOnRequest();
                }
            }
        }
    }

    private void checkResponseForPrebidCreative() {
        if (!TextUtils.isEmpty(adServerResponse) && (adServerResponse.contains("pbm.js") || adServerResponse.contains(
                "creative.js"))) {
            invokeContainsPrebidCreative(true);
        } else {
            invokeContainsPrebidCreative(false);
        }
    }

    private void invokeContainsPrebidCreative(@Nullable Boolean contains) {
        context.runOnUiThread(() -> {
            if (listener != null) {
                listener.adServerResponseContainsPrebidCreative(contains);
            }

            invokeTestFinished();
        });
    }

    private void invokeTestFinished() {
        if (googleAd != null) {
            googleAd.destroy();
        }

        if (listener != null) {
            listener.onTestFinished();
        }
    }
}
