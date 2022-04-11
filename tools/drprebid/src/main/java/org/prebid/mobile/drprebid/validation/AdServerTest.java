package org.prebid.mobile.drprebid.validation;

import static org.prebid.mobile.drprebid.managers.LineItemKeywordManager.KEYWORD_REQUEST_ID;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.prebid.mobile.drprebid.managers.LineItemKeywordManager;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdServerTest {
    private static final String TAG = AdServerTest.class.getSimpleName();

    public interface Listener {
        void onPrebidKeywordsFoundOnRequest();

        void onPrebidKeywordsNotFoundOnRequest();

        void adServerResponseContainsPrebidCreative(@Nullable Boolean contains);

        void onTestFinished();
    }

    private final Listener mListener;
    private final Activity mContext;


    private PublisherAdView mGoogleAd;
    private PublisherInterstitialAd mGoogleInterstitial;

    private String mAdServerResponse = "";
    private String mRequestId;
    private Map<String, String> mKeywords;

    public AdServerTest(Activity context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(mContext).getAdServerSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(mContext).getPrebidServerSettings();

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

        mKeywords = createMapKeywords(adServerSettings.getBidPrice(),
                generalSettings.getAdSize(), generalSettings.getAdFormat(), prebidServerSettings.getPrebidServer());

        PublisherAdRequest adRequest = null;

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            mGoogleAd = new PublisherAdView(mContext);
            AdSize adSize = generalSettings.getAdSize();
            mGoogleAd.setAdSizes(new com.google.android.gms.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
            mGoogleAd.setAdUnitId(adServerSettings.getAdUnitId());
            mGoogleAd.setAdListener(mGoogleBannerListener);

        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            mGoogleInterstitial = new PublisherInterstitialAd(mContext);
            mGoogleInterstitial.setAdUnitId(adServerSettings.getAdUnitId());
            mGoogleInterstitial.setAdListener(mGoogleInterstitialListener);
        }

        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

        for (String key : mKeywords.keySet()) {
            if (mKeywords.containsKey(key)) {
                adRequestBuilder.addCustomTargeting(key, mKeywords.get(key));
            }
        }

        adRequest = adRequestBuilder.build();

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            mGoogleAd.loadAd(adRequest);
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            mGoogleInterstitial.loadAd(adRequest);
        }

        checkRequestForKeywordsAM(adRequest);

    }

    private String createStringKeywords() {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : mKeywords.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(LineItemKeywordManager.KEYWORD_COMMA);
            }

            String keyword = String.format(Locale.ENGLISH, "%s:%s", key, mKeywords.get(key));
            stringBuilder.append(keyword);
        }

        return stringBuilder.toString();
    }

    private Map<String, String> createMapKeywords(float bidPrice, AdSize adSize, AdFormat adFormat, PrebidServer prebidServer) {
        Map<String, String> keywords = new HashMap<>(LineItemKeywordManager.getInstance().getMapKeywords(bidPrice, adSize, adFormat, prebidServer));

        mRequestId = UUID.randomUUID().toString();

        keywords.put(KEYWORD_REQUEST_ID, mRequestId);

        return keywords;
    }

    //--------------------------------- Google Banner Listener -------------------------------------

    private AdListener mGoogleBannerListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            AdViewUtils.findHtml(mGoogleAd, new OnWebViewListener() {
                @Override
                public void success(String html) {
                    mAdServerResponse = html;
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

    private AdListener mGoogleInterstitialListener = new AdListener() {
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
        if (adRequest != null && mListener != null) {

            Map<String, String> map = new HashMap<>();

            Set<String> set = adRequest.getCustomTargeting().keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                map.put(key, adRequest.getCustomTargeting().getString(key));
            }

            if (map.get(KEYWORD_REQUEST_ID).equals(mRequestId)) {
                if (mKeywords.equals(map)) {
                    mListener.onPrebidKeywordsFoundOnRequest();
                } else {
                    mListener.onPrebidKeywordsNotFoundOnRequest();
                }
            }
        }
    }

    private void checkRequestForKeywords(String url, String postBody) {
        if (url.contains(mRequestId) || postBody.contains(mRequestId)) {
            boolean containsKeyValues = true;

            for (String key : mKeywords.keySet()) {
                String keyValuePair = String.format(Locale.ENGLISH, "%s:%s", key, mKeywords.get(key));
                if (!postBody.contains(keyValuePair)) {
                    containsKeyValues = false;
                }
            }

            if (mListener != null) {
                if (containsKeyValues) {
                    mListener.onPrebidKeywordsFoundOnRequest();
                } else {
                    mListener.onPrebidKeywordsNotFoundOnRequest();
                }
            }
        }
    }

    private void checkResponseForPrebidCreative() {
        if (!TextUtils.isEmpty(mAdServerResponse) && (mAdServerResponse.contains("pbm.js") || mAdServerResponse.contains("creative.js"))) {
            invokeContainsPrebidCreative(true);
        } else {
            invokeContainsPrebidCreative(false);
        }
    }

    private void invokeContainsPrebidCreative(@Nullable Boolean contains) {
        mContext.runOnUiThread(() -> {
            if (mListener != null) {
                mListener.adServerResponseContainsPrebidCreative(contains);
            }

            invokeTestFinished();
        });
    }

    private void invokeTestFinished() {
        if (mGoogleAd != null) {
            mGoogleAd.destroy();
        }

        if (mListener != null) {
            mListener.onTestFinished();
        }
    }
}
