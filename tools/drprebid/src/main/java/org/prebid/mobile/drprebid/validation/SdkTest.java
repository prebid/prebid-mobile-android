package org.prebid.mobile.drprebid.validation;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

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
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.prebid.mobile.ResultCode.SUCCESS;

public class SdkTest implements MoPubView.BannerAdListener, MoPubInterstitial.InterstitialAdListener {
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

    private MoPubView moPubBanner;
    private MoPubInterstitial moPubInterstitial;

    private MoPubRequestQueue moPubRequestQueue;

    public SdkTest(
            Activity context,
            Listener listener
    ) {
        this.context = context;
        this.listener = listener;

        moPubRequestQueue = Networking.getRequestQueue(context);
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

    private void destroy() {
        moPubRequestQueue.removeRequestFinishedListener(moPubRequestFinishedListener);
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(context).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(context).getAdServerSettings();

        if (adServerSettings.getAdServer() == AdServer.MOPUB) {
            if (generalSettings.getAdFormat() == AdFormat.BANNER) {
                AdSize adSize = generalSettings.getAdSize();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(DimenUtil.convertPxToDp(
                        context,
                        adSize.getWidth()
                ),
                        DimenUtil.convertPxToDp(context, adSize.getHeight())
                );
                moPubBanner = new MoPubView(context);
                moPubBanner.setLayoutParams(layoutParams);
                moPubBanner.setAutorefreshEnabled(false);
                moPubBanner.setBannerAdListener(this);
                moPubBanner.setAdUnitId(adServerSettings.getAdUnitId());

                if (adUnit != null) {
                    Networking.getRequestQueue(context).addRequestFinishedListener(moPubRequestFinishedListener);
                    adUnit.fetchDemand(moPubBanner, resultCode -> {
                        checkPrebidLog();
                        moPubBanner.loadAd();
                    });

                    if (listener != null) {
                        listener.requestToPrebidServerSent(true);
                    }
                }
            } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
                moPubInterstitial = new MoPubInterstitial(context, adServerSettings.getAdUnitId());
                moPubInterstitial.setInterstitialAdListener(this);

                if (adUnit != null) {
                    Networking.getRequestQueue(context).addRequestFinishedListener(moPubRequestFinishedListener);
                    adUnit.fetchDemand(moPubInterstitial, resultCode -> {
                        checkPrebidLog();
                        moPubInterstitial.load();
                    });

                    if (listener != null) {
                        listener.requestToPrebidServerSent(true);
                    }
                }
            }
        } else if (adServerSettings.getAdServer() == AdServer.GOOGLE_AD_MANAGER) {
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

    //---------------------------------- MoPub Banner Listener -------------------------------------

    private final RequestQueue.RequestFinishedListener moPubRequestFinishedListener = request -> {
        if (request != null) {
            processMoPubRequest(request);
        }
    };

    private void processMoPubRequest(Request request) {
        Networking.getRequestQueue(context).removeRequestFinishedListener(moPubRequestFinishedListener);

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
                            adServerResponse = IOUtil.getStringFromStream(inputStream);
                            inputStream.close();
                            checkResponseForPrebidCreative();
                        } else {
                            invokeContainsPrebidCreative(false);
                        }
                    }
                });

                context.runOnUiThread(() -> {
                    if (listener != null) {
                        listener.requestSentToAdServer(url, postBody);
                    }
                });
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
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

                listener.onTestFinished();
            }
        });
    }
}
