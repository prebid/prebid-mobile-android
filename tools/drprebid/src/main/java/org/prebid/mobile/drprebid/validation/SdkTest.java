package org.prebid.mobile.drprebid.validation;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import org.prebid.mobile.*;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.*;

import javax.net.ssl.HttpsURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.prebid.mobile.ResultCode.SUCCESS;

public class SdkTest {
    private static final String TAG = SdkTest.class.getSimpleName();

    public interface Listener {
        void onAdUnitRegistered();

        void requestToPrebidServerSent(boolean sent);

        void responseFromPrebidServerReceived(boolean received);

        void bidReceivedAndCached(boolean received);

        void requestSentToAdServer(
                String request,
                String postBody
        );

        void adServerResponseContainsPrebidCreative(@Nullable Boolean contains);

        void onTestFinished();

    }

    private Listener listener;
    private Activity context;

    private AdUnit adUnit;
    private boolean initialPrebidServerResponseReceived;
    private String adServerResponse = "";

    private PublisherAdView googleBanner;
    private PublisherInterstitialAd googleInterstitial;
    private PublisherAdRequest googleAdRequest;


    public SdkTest(
            Activity context,
            Listener listener
    ) {
        this.context = context;
        this.listener = listener;

        setupPrebid();
    }

    private void setupPrebid() {
        GeneralSettings generalSettings = SettingsManager.getInstance(context).getGeneralSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(context).getPrebidServerSettings();

        setPrebidTargetingParams();

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            AdSize adSize = generalSettings.getAdSize();
            adUnit = new BannerAdUnit(prebidServerSettings.getConfigId(), adSize.getWidth(), adSize.getHeight());
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            adUnit = new InterstitialAdUnit(prebidServerSettings.getConfigId());
        }

        PrebidMobile.setPrebidServerAccountId(prebidServerSettings.getAccountId());

        switch (prebidServerSettings.getPrebidServer()) {
            case APPNEXUS:
                PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
                break;
            case RUBICON:
                PrebidMobile.setPrebidServerHost(Host.RUBICON);
                break;
            case CUSTOM:
                PrebidMobile.setPrebidServerHost(Host.CUSTOM);
                Host.CUSTOM.setHostUrl(buildCustomServerEndpoint(prebidServerSettings.getCustomPrebidServerUrl()));
                break;
        }
        PrebidMobile.initializeSdk(context, null);

        if (listener != null) {
            listener.onAdUnitRegistered();
        }
    }

    private String buildCustomServerEndpoint(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
            uriBuilder.appendPath("openrtb2");
            uriBuilder.appendPath("auction");

            return uriBuilder.build().toString();
        } else {
            return "";
        }
    }

    private void setPrebidTargetingParams() {
        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
        PrebidMobile.setShareGeoLocation(true);
    }


    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(context).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(context).getAdServerSettings();


        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            googleBanner = new PublisherAdView(context);
            AdSize adSize = generalSettings.getAdSize();
            googleBanner.setAdSizes(new com.google.android.gms.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
            googleBanner.setAdUnitId(adServerSettings.getAdUnitId());
            googleBanner.setAdListener(googleBannerListener);
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            googleInterstitial = new PublisherInterstitialAd(context);
            googleInterstitial.setAdUnitId(adServerSettings.getAdUnitId());
            googleInterstitial.setAdListener(googleInterstitialListener);
        }

        if (adUnit != null) {
            final Map<String, String> keywordsMap = new HashMap<>();

            adUnit.fetchDemand(keywordsMap, resultCode -> {

                boolean responseSuccess = false;
                if (resultCode == SUCCESS) {
                    responseSuccess = true;
                }

                boolean topBid = false;
                if (keywordsMap.containsKey("hb_cache_id")) {
                    topBid = true;
                }

                PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();

                for (Map.Entry<String, String> entry : keywordsMap.entrySet()) {
                    adRequestBuilder.addCustomTargeting(entry.getKey(), entry.getValue());
                }

                if (generalSettings.getAdFormat() == AdFormat.BANNER) {
                    googleBanner.loadAd(adRequestBuilder.build());
                } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                    googleInterstitial.loadAd(adRequestBuilder.build());
                }

                if (listener != null) {
                    listener.responseFromPrebidServerReceived(responseSuccess);
                    listener.bidReceivedAndCached(topBid);
                    listener.requestSentToAdServer("", "");
                }

            });

            if (listener != null) {
                listener.requestToPrebidServerSent(true);
            }
        }

    }

    private void checkPrebidLog() {
        if (!initialPrebidServerResponseReceived) {
            BidLog.BidLogEntry entry = BidLog.getInstance().getLastBid();

            if (listener != null) {
                if (entry != null) {
                    if (entry.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        listener.responseFromPrebidServerReceived(true);
                        if (entry.containsTopBid()) {
                            listener.bidReceivedAndCached(true);
                        } else {
                            listener.bidReceivedAndCached(false);
                        }
                    } else {
                        listener.responseFromPrebidServerReceived(false);
                        listener.bidReceivedAndCached(false);
                    }
                } else {
                    listener.responseFromPrebidServerReceived(false);
                    listener.bidReceivedAndCached(false);
                }
            }

            BidLog.getInstance().cleanLog();

            initialPrebidServerResponseReceived = true;
        }
    }



    //--------------------------------- Google Banner Listener -------------------------------------

    private AdListener googleBannerListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            AdViewUtils.findHtml(googleBanner, new OnWebViewListener() {
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
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);

            invokeContainsPrebidCreative(false);
        }
    };



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

                listener.onTestFinished();
            }
        });
    }
}
