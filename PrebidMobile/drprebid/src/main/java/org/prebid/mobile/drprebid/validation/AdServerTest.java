package org.prebid.mobile.drprebid.validation;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.mopub.network.Networking;
import com.mopub.volley.Request;
import com.mopub.volley.RequestQueue;

import org.prebid.mobile.drprebid.managers.LineItemKeywordManager;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdServer;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.AdServerTestResult;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.util.DimenUtil;
import org.prebid.mobile.drprebid.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdServerTest implements MoPubView.BannerAdListener, MoPubInterstitial.InterstitialAdListener {
    private static final String TAG = AdServerTest.class.getSimpleName();

    public interface Listener {
        void onPrebidKeywordsFoundOnRequest();

        void onPrebidKeywordsNotFoundOnRequest();

        void onServerRespondedWithPrebidCreative();

        void onServerNotRespondedWithPrebidCreative();

        void onTestFinished();
    }

    private final Listener mListener;
    private final Activity mContext;

    private MoPubView mMoPubAd;
    private MoPubInterstitial mMoPubInterstitial;
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

        if (adServerSettings.getAdServer() == AdServer.MOPUB) {
            String keywords = createStringKeywords();

            if (generalSettings.getAdFormat() == AdFormat.BANNER) {
                AdSize adSize = generalSettings.getAdSize();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        DimenUtil.convertPxToDp(mContext, adSize.getWidth()),
                        DimenUtil.convertPxToDp(mContext, adSize.getHeight()));
                mMoPubAd = new MoPubView(mContext);
                mMoPubAd.setLayoutParams(layoutParams);
                mMoPubAd.setAdUnitId(adServerSettings.getAdUnitId());
                mMoPubAd.setAutorefreshEnabled(false);
                mMoPubAd.setBannerAdListener(this);
                mMoPubAd.setKeywords(keywords);

                Networking.getRequestQueue(mContext).addRequestFinishedListener(mMoPubRequestFinishedListener);

                mMoPubAd.loadAd();
            } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                mMoPubInterstitial = new MoPubInterstitial(mContext, adServerSettings.getAdUnitId());
                mMoPubInterstitial.setInterstitialAdListener(this);
                mMoPubInterstitial.setKeywords(keywords);

                Networking.getRequestQueue(mContext).addRequestFinishedListener(mMoPubRequestFinishedListener);

                mMoPubInterstitial.load();
            }
        } else if (adServerSettings.getAdServer() == AdServer.GOOGLE_AD_MANAGER) {
            if (generalSettings.getAdFormat() == AdFormat.BANNER) {
                mGoogleAd = new PublisherAdView(mContext);
                AdSize adSize = generalSettings.getAdSize();
                mGoogleAd.setAdSizes(new com.google.android.gms.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
                mGoogleAd.setAdUnitId(adServerSettings.getAdUnitId());
                mGoogleAd.setAdListener(mGoogleBannerListener);

                PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

                for (String key : mKeywords.keySet()) {
                    if (mKeywords.containsKey(key)) {
                        adRequestBuilder.addCustomTargeting(key, mKeywords.get(key));
                    }
                }

                PublisherAdRequest adRequest = adRequestBuilder.build();
                mGoogleAd.loadAd(adRequest);

            } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                 mGoogleInterstitial = new PublisherInterstitialAd(mContext);
                mGoogleInterstitial.setAdListener(mGoogleInterstitialListener);

                PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

                for (String key : mKeywords.keySet()) {
                    if (mKeywords.containsKey(key)) {
                        adRequestBuilder.addCustomTargeting(key, mKeywords.get(key));
                    }
                }

                PublisherAdRequest adRequest = adRequestBuilder.build();
                mGoogleInterstitial.loadAd(adRequest);
            }
        }
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

        keywords.put(LineItemKeywordManager.KEYWORD_REQUEST_ID, mRequestId);

        return keywords;
    }

    //--------------------------------- Google Banner Listener -------------------------------------

    private AdListener mGoogleBannerListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);
        }
    };

    //------------------------------- Google Interstitial Listener ---------------------------------

    private AdListener mGoogleInterstitialListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);
        }
    };

    //---------------------------------- MoPub Banner Listener -------------------------------------

    @Override
    public void onBannerLoaded(MoPubView banner) {

    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {

    }

    @Override
    public void onBannerClicked(MoPubView banner) {

    }

    @Override
    public void onBannerExpanded(MoPubView banner) {

    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {

    }

    //------------------------------- MoPub Interstitial Listener ----------------------------------

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {

    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {

    }

    private final RequestQueue.RequestFinishedListener mMoPubRequestFinishedListener = request -> {
        if (request != null) {
            processMoPubRequest(request);
        }
    };

    private void processMoPubRequest(Request request) {
        Networking.getRequestQueue(mContext).removeRequestFinishedListener(mMoPubRequestFinishedListener);

        try {
            String url = request.getUrl();

            if (!TextUtils.isEmpty(url) && request.getBody() != null) {
                ByteArrayInputStream postBodyStream = new ByteArrayInputStream(request.getBody());
                String postBody = IOUtil.getStringFromStream(postBodyStream);
                postBodyStream.close();

                checkRequestForKeywords(url, postBody);

                OkHttpClient client = new OkHttpClient.Builder().build();

                RequestBody body = RequestBody.create(MediaType.parse(request.getBodyContentType()), postBody);

                okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                client.newCall(httpRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, e.getMessage());
                        invokeDoesNotContainPrebidCreative();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            InputStream inputStream = response.body().byteStream();
                            mAdServerResponse = IOUtil.getStringFromStream(inputStream);
                            inputStream.close();
                            checkResponseForPrebidCreative();
                        } else {
                            invokeDoesNotContainPrebidCreative();
                        }
                    }
                });
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
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
            invokeContainsPrebidCreative();
        } else {
            invokeDoesNotContainPrebidCreative();
        }
    }

    private void invokeContainsPrebidCreative() {
        mContext.runOnUiThread(() -> {
            if (mListener != null) {
                mListener.onServerRespondedWithPrebidCreative();
            }

            invokeTestFinished();
        });
    }

    private void invokeDoesNotContainPrebidCreative() {
        mContext.runOnUiThread(() -> {
            if (mListener != null) {
                mListener.onServerNotRespondedWithPrebidCreative();
            }

            invokeTestFinished();
        });
    }

    private void invokeTestFinished() {
        if (mMoPubAd != null) {
            mMoPubAd.destroy();
        }

        if (mMoPubInterstitial != null) {
            mMoPubInterstitial.destroy();
        }

        if (mGoogleAd != null) {
            mGoogleAd.destroy();
        }

        if (mListener != null) {
            mListener.onTestFinished();
        }
    }
}
