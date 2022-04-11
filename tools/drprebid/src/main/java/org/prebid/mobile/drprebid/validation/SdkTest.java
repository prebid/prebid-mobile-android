package org.prebid.mobile.drprebid.validation;

import static org.prebid.mobile.ResultCode.SUCCESS;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BidLog;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SdkTest {
    private static final String TAG = SdkTest.class.getSimpleName();

    public interface Listener {
        void onAdUnitRegistered();

        void requestToPrebidServerSent(boolean sent);

        void responseFromPrebidServerReceived(boolean received);

        void bidReceivedAndCached(boolean received);

        void requestSentToAdServer(String request, String postBody);

        void adServerResponseContainsPrebidCreative(@Nullable Boolean contains);

        void onTestFinished();
    }

    private Listener mListener;
    private Activity mContext;

    private AdUnit mAdUnit;
    private boolean mInitialPrebidServerResponseReceived;
    private String mAdServerResponse = "";

    private PublisherAdView mGoogleBanner;
    private PublisherInterstitialAd mGoogleInterstitial;
    private PublisherAdRequest mGoogleAdRequest;


    public SdkTest(Activity context, Listener listener) {
        mContext = context;
        mListener = listener;

        setupPrebid();
    }

    private void setupPrebid() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(mContext).getPrebidServerSettings();

        setPrebidTargetingParams();

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            AdSize adSize = generalSettings.getAdSize();
            mAdUnit = new BannerAdUnit(prebidServerSettings.getConfigId(), adSize.getWidth(), adSize.getHeight());
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            mAdUnit = new InterstitialAdUnit(prebidServerSettings.getConfigId());
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

        if (mListener != null) {
            mListener.onAdUnitRegistered();
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
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(mContext).getAdServerSettings();


        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            mGoogleBanner = new PublisherAdView(mContext);
            AdSize adSize = generalSettings.getAdSize();
            mGoogleBanner.setAdSizes(new com.google.android.gms.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
            mGoogleBanner.setAdUnitId(adServerSettings.getAdUnitId());
            mGoogleBanner.setAdListener(mGoogleBannerListener);

        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            mGoogleInterstitial = new PublisherInterstitialAd(mContext);
            mGoogleInterstitial.setAdUnitId(adServerSettings.getAdUnitId());
            mGoogleInterstitial.setAdListener(mGoogleInterstitialListener);
        }

        if (mAdUnit != null) {
            final Map<String, String> keywordsMap = new HashMap<>();

            mAdUnit.fetchDemand(keywordsMap, resultCode -> {

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
                    mGoogleBanner.loadAd(adRequestBuilder.build());
                } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                    mGoogleInterstitial.loadAd(adRequestBuilder.build());
                }

                if (mListener != null) {
                    mListener.responseFromPrebidServerReceived(responseSuccess);
                    mListener.bidReceivedAndCached(topBid);
                    mListener.requestSentToAdServer("", "");
                }

            });

            if (mListener != null) {
                mListener.requestToPrebidServerSent(true);
            }

        }

    }

    private void checkPrebidLog() {
        if (!mInitialPrebidServerResponseReceived) {
            BidLog.BidLogEntry entry = BidLog.getInstance().getLastBid();

            if (mListener != null) {
                if (entry != null) {
                    if (entry.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        mListener.responseFromPrebidServerReceived(true);
                        if (entry.containsTopBid()) {
                            mListener.bidReceivedAndCached(true);
                        } else {
                            mListener.bidReceivedAndCached(false);
                        }
                    } else {
                        mListener.responseFromPrebidServerReceived(false);
                        mListener.bidReceivedAndCached(false);
                    }
                } else {
                    mListener.responseFromPrebidServerReceived(false);
                    mListener.bidReceivedAndCached(false);
                }
            }

            BidLog.getInstance().cleanLog();

            mInitialPrebidServerResponseReceived = true;
        }
    }



    //--------------------------------- Google Banner Listener -------------------------------------

    private AdListener mGoogleBannerListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            AdViewUtils.findHtml(mGoogleBanner, new OnWebViewListener() {
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
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            super.onAdFailedToLoad(errorCode);

            invokeContainsPrebidCreative(false);
        }
    };

    //---------------------------------- MoPub Banner Listener -------------------------------------


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

                mListener.onTestFinished();
            }
        });
    }
}
