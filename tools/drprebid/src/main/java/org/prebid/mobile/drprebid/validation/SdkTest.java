package org.prebid.mobile.drprebid.validation;

import android.app.Activity;
import android.net.Uri;
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
import com.mopub.network.MoPubRequestQueue;
import com.mopub.network.Networking;
import com.mopub.volley.Request;
import com.mopub.volley.RequestQueue;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BidLog;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdServer;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.util.DimenUtil;
import org.prebid.mobile.drprebid.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SdkTest implements MoPubView.BannerAdListener, MoPubInterstitial.InterstitialAdListener {
    private static final String TAG = SdkTest.class.getSimpleName();

    public interface Listener {
        void onAdUnitRegistered();

        void requestToPrebidServerSent(boolean sent);

        void responseFromPrebidServerReceived(boolean received);

        void bidReceivedAndCached(boolean received);

        void requestSentToAdServer(String request, String postBody);

        void adServerResponseContainsPrebidCreative(boolean contains);

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

    private MoPubView mMoPubBanner;
    private MoPubInterstitial mMoPubInterstitial;

    private MoPubRequestQueue mMoPubRequestQueue;

    public SdkTest(Activity context, Listener listener) {
        mContext = context;
        mListener = listener;

        mMoPubRequestQueue = Networking.getRequestQueue(context);
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

    private void destroy() {
        mMoPubRequestQueue.removeRequestFinishedListener(mMoPubRequestFinishedListener);
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(mContext).getAdServerSettings();

        if (adServerSettings.getAdServer() == AdServer.MOPUB) {
            if (generalSettings.getAdFormat() == AdFormat.BANNER) {
                AdSize adSize = generalSettings.getAdSize();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        DimenUtil.convertPxToDp(mContext, adSize.getWidth()),
                        DimenUtil.convertPxToDp(mContext, adSize.getHeight()));
                mMoPubBanner = new MoPubView(mContext);
                mMoPubBanner.setLayoutParams(layoutParams);
                mMoPubBanner.setAutorefreshEnabled(false);
                mMoPubBanner.setBannerAdListener(this);
                mMoPubBanner.setAdUnitId(adServerSettings.getAdUnitId());

                if (mAdUnit != null) {
                    Networking.getRequestQueue(mContext).addRequestFinishedListener(mMoPubRequestFinishedListener);
                    mAdUnit.fetchDemand(mMoPubBanner, resultCode -> {
                        checkPrebidLog();
                        mMoPubBanner.loadAd();
                    });

                    if (mListener != null) {
                        mListener.requestToPrebidServerSent(true);
                    }
                }

            } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                mMoPubInterstitial = new MoPubInterstitial(mContext, adServerSettings.getAdUnitId());
                mMoPubInterstitial.setInterstitialAdListener(this);

                if (mAdUnit != null) {
                    Networking.getRequestQueue(mContext).addRequestFinishedListener(mMoPubRequestFinishedListener);
                    mAdUnit.fetchDemand(mMoPubInterstitial, resultCode -> {
                        checkPrebidLog();
                        mMoPubInterstitial.load();
                    });

                    if (mListener != null) {
                        mListener.requestToPrebidServerSent(true);
                    }
                }
            }
        } else if (adServerSettings.getAdServer() == AdServer.GOOGLE_AD_MANAGER) {
            if (generalSettings.getAdFormat() == AdFormat.BANNER) {
                mGoogleBanner = new PublisherAdView(mContext);
                AdSize adSize = generalSettings.getAdSize();
                mGoogleBanner.setAdSizes(new com.google.android.gms.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
                mGoogleBanner.setAdUnitId(adServerSettings.getAdUnitId());
                mGoogleBanner.setAdListener(mGoogleBannerListener);

                PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

                if (mAdUnit != null) {
                    mAdUnit.fetchDemand(mGoogleBanner, resultCode -> mGoogleBanner.loadAd(adRequest));

                    if (mListener != null) {
                        mListener.requestToPrebidServerSent(true);
                    }
                }

            } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                mGoogleInterstitial = new PublisherInterstitialAd(mContext);
                mGoogleInterstitial.setAdListener(mGoogleInterstitialListener);

                PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

                if (mAdUnit != null) {
                    mAdUnit.fetchDemand(mGoogleInterstitial, resultCode -> mGoogleInterstitial.loadAd(adRequest));

                    if (mListener != null) {
                        mListener.requestToPrebidServerSent(true);
                    }
                }
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

    //---------------------------------- MoPub Banner Listener -------------------------------------

    @Override
    public void onBannerLoaded(MoPubView banner) {
        checkResponseForPrebidCreative();
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        invokeContainsPrebidCreative(false);
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
        checkResponseForPrebidCreative();
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        invokeContainsPrebidCreative(false);
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
                        invokeContainsPrebidCreative(false);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            InputStream inputStream = response.body().byteStream();
                            mAdServerResponse = IOUtil.getStringFromStream(inputStream);
                            inputStream.close();
                            checkResponseForPrebidCreative();
                        } else {
                            invokeContainsPrebidCreative(false);
                        }
                    }
                });

                mContext.runOnUiThread(() -> {
                    if (mListener != null) {
                        mListener.requestSentToAdServer(url, postBody);
                    }
                });

            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    private void checkResponseForPrebidCreative() {
        if (!TextUtils.isEmpty(mAdServerResponse) && (mAdServerResponse.contains("pbm.js") || mAdServerResponse.contains("creative.js"))) {
            invokeContainsPrebidCreative(true);
        } else {
            invokeContainsPrebidCreative(false);
        }
    }

    private void invokeContainsPrebidCreative(boolean contains) {
        mContext.runOnUiThread(() -> {
            if (mListener != null) {
                mListener.adServerResponseContainsPrebidCreative(contains);

                mListener.onTestFinished();
            }
        });
    }
}
