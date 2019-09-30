/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.video.ima;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.VideoInterstitialAdUnit;
import org.prebid.mobile.VideoUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class VideoImaView extends FrameLayout implements ImaAdapter.VideoImaDelegate, ImaAdapter.VideoImaLoaderDelegate, Serializable {

    private TextView muteSwitcher;

    @Nullable
    private PbVideoAdDelegate pbVideoAdDelegate;
    private ImaAdapter imaAdapter;
    private ImaSdkFactory sdkFactory;
    @Nullable
    private AdsLoader adsLoader;
    @Nullable
    private AdsManager adsManager;

    private boolean adCanBePlayed = false;

    public VideoImaView(Context context) {
        super(context);

        initialization();
    }

    public VideoImaView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialization();
    }

    public VideoImaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialization();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoImaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialization();
    }

    public void setVideoAdDelegate(@Nullable PbVideoAdDelegate pbVideoAdDelegate) {
        this.pbVideoAdDelegate = pbVideoAdDelegate;
        imaAdapter.setPbVideoAdDelegate(pbVideoAdDelegate);
    }

    //public zone
    public void loadAd(VideoAdUnit videoAdUnit, String adUnitId) {

        loadAdAndAutoPlay(videoAdUnit, adUnitId);
    }

    public void reset() {

        resetAdsManager();
        resetAdsLoader();
        resetView();
    }

    public void loadAdAndAutoPlay(AdUnit adUnit, String adUnitId) {

        adCanBePlayed = true;

        makeAuctionAndLoadAd(adUnit, adUnitId);
    }

    public void loadAdWithoutAutoPlay(AdUnit adUnit, String adUnitId) {

        adCanBePlayed = false;

        makeAuctionAndLoadAd(adUnit, adUnitId);
    }

    //default zone
    void addVideoImaDelegate(ImaAdapter.VideoImaDelegate videoImaDelegate) {
        imaAdapter.addVideoImaDelegate(videoImaDelegate);
    }

    void remove(ImaAdapter.VideoImaDelegate videoImaDelegate) {
        imaAdapter.removeVideoImaDelegate(videoImaDelegate);
    }

    void setAutoPlayAndShowAd() {
        adCanBePlayed = true;
        checkAutoPlayAndShowAd();
    }

    @Nullable
    public AdsManager getAdsManager() {
        return adsManager;
    }

    //private zone
    private void initialization() {
        initMuteSwitcher();
        initImaAdapter();
    }

    private void initMuteSwitcher() {
        muteSwitcher = new TextView(getContext());
        muteSwitcher.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.argb(50, 0, 0, 0));
        shape.setCornerRadius(20);

        muteSwitcher.setBackgroundDrawable(shape);

        muteSwitcher.setPadding(20, 20, 20, 20);
        muteSwitcher.setText("Unmute");
        muteSwitcher.setTextColor(Color.WHITE);
        muteSwitcher.setTextSize(15);
        muteSwitcher.setAllCaps(false);

        muteSwitcher.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isMuted = isMuted();
                isMuted = !isMuted;

                String buttonTitle = "Mute";
                if (isMuted) {
                    buttonTitle = "Unmute";
                }

                setMute(isMuted);

                muteSwitcher.setText(buttonTitle);

            }
        });

    }

    private void initImaAdapter() {
        imaAdapter = new ImaAdapter();
        imaAdapter.setVideoImaLoaderDelegate(this);
        imaAdapter.addVideoImaDelegate(this);
    }

    private boolean isMuted() {
        AudioManager audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
    }

    private void setMute(boolean state) {

        AudioManager audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, state);
    }

    private void showMuteSwitcher() {
        this.addView(muteSwitcher);
    }

    private void setup() {

        setupAdsLoader();
        setMute(true);
    }

    private void setupAdsLoader() {
        // Create an AdsLoader.
        sdkFactory = ImaSdkFactory.getInstance();
        AdDisplayContainer adDisplayContainer = sdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(this);
        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();

        adsLoader = sdkFactory.createAdsLoader(this.getContext(), settings, adDisplayContainer);
        // Add listeners for when ads are loaded and for errors.
        adsLoader.addAdErrorListener(imaAdapter);
        adsLoader.addAdsLoadedListener(imaAdapter);
    }

    private void setupAdsManager(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
        // events for ad playback and errors.
        adsManager = adsManagerLoadedEvent.getAdsManager();

        // Attach event and error event listeners.
        adsManager.addAdErrorListener(imaAdapter);
        adsManager.addAdEventListener(imaAdapter);
        adsManager.init();
    }

    private void makeAuctionAndLoadAd(final AdUnit adUnit, final String adUnitId) {

        reset();
        setup();

        final HashMap targetingMap = new HashMap();

        adUnit.fetchDemand(targetingMap, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                String resultCodeName = resultCode.name();

                if (resultCode == ResultCode.SUCCESS) {

                    String adSlotSize = null;
                    if (adUnit instanceof VideoAdUnit) {
                        adSlotSize = ((VideoAdUnit) adUnit).getAdSizeString();
                    } else if (adUnit instanceof VideoInterstitialAdUnit) {
                        adSlotSize = ((VideoInterstitialAdUnit) adUnit).getAddSizeString();
                    }

                    String adTagUrl = VideoUtils.buildAdTagUrl(adUnitId, adSlotSize, targetingMap);
                    loadAd(adTagUrl);
                } else {

                    if (pbVideoAdDelegate != null) {
                        pbVideoAdDelegate.videoAdEvent(VideoAdEventFactory.getAdLoadFail(resultCodeName));
                    }
                }

            }
        });

    }

    private void loadAd(String adTagUrl) {

        AdsRequest request = sdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);

        if (adsLoader != null) {
            adsLoader.requestAds(request);

        }
    }

    private void checkAutoPlayAndShowAd() {
        if (adCanBePlayed && adsManager != null) {
            adsManager.start();
        }
    }

    private void resetAdsManager() {
        if (adsManager != null) {
            adsManager.destroy();
            adsManager = null;
        }
    }

    private void resetAdsLoader() {
        if (adsLoader != null) {
            adsLoader.removeAdErrorListener(imaAdapter);
            adsLoader.removeAdsLoadedListener(imaAdapter);
            adsLoader = null;
        }
    }

    private void resetView() {
        this.removeAllViews();
    }

    //VideoImaLoaderDelegate
    @Override
    public void adDidLoad(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        setupAdsManager(adsManagerLoadedEvent);
    }

    //VideoImaDelegate
    @Override
    public void adLoaded() {
        checkAutoPlayAndShowAd();
    }

    @Override
    public void adStarted() {
        showMuteSwitcher();
    }

    @Override
    public void adSkipped() {
    }

    @Override
    public void adFinished() {
    }

    @Override
    public void adPlayingFailed() {
    }

    @Override
    public void allAdsCompleted() {
        resetView();
    }

}

class ImaAdapter implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

    interface VideoImaLoaderDelegate {
        void adDidLoad(AdsManagerLoadedEvent adsManagerLoadedEvent);
    }

    interface VideoImaDelegate {
        void adLoaded();
        void adStarted();
        void adSkipped();
        void adFinished();
        void adPlayingFailed();
        void allAdsCompleted();
    }

    @Nullable
    private VideoImaLoaderDelegate videoImaLoaderDelegate;
    private final List<VideoImaDelegate> videoImaDelegateStorage = new ArrayList<>(1);
    @Nullable
    private PbVideoAdDelegate pbVideoAdDelegate;

    void setPbVideoAdDelegate(PbVideoAdDelegate pbVideoAdDelegate) {
        this.pbVideoAdDelegate = pbVideoAdDelegate;
    }

    ImaAdapter() {
    }


    public void setVideoImaLoaderDelegate(VideoImaLoaderDelegate videoImaLoaderDelegate) {
        this.videoImaLoaderDelegate = videoImaLoaderDelegate;
    }

    void addVideoImaDelegate(VideoImaDelegate videoImaDelegate) {
        this.videoImaDelegateStorage.add(videoImaDelegate);
    }

    void removeVideoImaDelegate(VideoImaDelegate videoImaDelegate) {
        this.videoImaDelegateStorage.remove(videoImaDelegate);
    }

    //AdsLoadedListener
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        if (videoImaLoaderDelegate == null) {
            return;
        }
        videoImaLoaderDelegate.adDidLoad(adsManagerLoadedEvent);

    }

    //AdEventListener
    @Override
    public void onAdEvent(AdEvent adEvent) {
        LogUtil.d("AdEventListener onAdEvent: " + adEvent.getType());

        String eventString = adEvent.getType().name();
        switch (adEvent.getType()) {
            case LOADED:
                iterateVideoImaDelegates(new OnVideoImaDelegate() {
                    @Override
                    public void closure(VideoImaDelegate videoImaDelegate) {
                        videoImaDelegate.adLoaded();
                    }
                });

                if (pbVideoAdDelegate != null) {
                    pbVideoAdDelegate.videoAdEvent(VideoAdEventFactory.getAdLoadSuccess(eventString));
                }

                break;

            case STARTED:
                iterateVideoImaDelegates(new OnVideoImaDelegate() {
                    @Override
                    public void closure(VideoImaDelegate videoImaDelegate) {
                        videoImaDelegate.adStarted();
                    }
                });

                if (pbVideoAdDelegate != null) {
                    pbVideoAdDelegate.videoAdEvent(VideoAdEventFactory.getAdStarted(eventString));
                }

                break;

            case COMPLETED:
                iterateVideoImaDelegates(new OnVideoImaDelegate() {
                    @Override
                    public void closure(VideoImaDelegate videoImaDelegate) {
                        videoImaDelegate.adFinished();
                    }
                });

                if (pbVideoAdDelegate != null) {
                    pbVideoAdDelegate.videoAdEvent(VideoAdEventFactory.getAdDidReachEnd(eventString));
                }

                break;

            case CLICKED:
                if (pbVideoAdDelegate != null) {
                    pbVideoAdDelegate.videoAdEvent(VideoAdEventFactory.getAdClicked(eventString));
                }
                break;

            case SKIPPED:
                iterateVideoImaDelegates(new OnVideoImaDelegate() {
                    @Override
                    public void closure(VideoImaDelegate videoImaDelegate) {
                        videoImaDelegate.adSkipped();
                    }
                });
                break;

            case ALL_ADS_COMPLETED:
                iterateVideoImaDelegates(new OnVideoImaDelegate() {
                    @Override
                    public void closure(VideoImaDelegate videoImaDelegate) {
                        videoImaDelegate.allAdsCompleted();
                    }
                });
                break;

            default:
                break;
        }
    }

    //AdErrorListener
    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {

        iterateVideoImaDelegates(new OnVideoImaDelegate() {
            @Override
            public void closure(VideoImaDelegate videoImaDelegate) {
                videoImaDelegate.adPlayingFailed();
            }
        });

        if (pbVideoAdDelegate != null) {
            pbVideoAdDelegate.videoAdEvent(VideoAdEventFactory.getAdLoadFail(adErrorEvent.getError().getMessage()));
        }
    }

    private interface OnVideoImaDelegate {
        void closure(VideoImaDelegate videoImaDelegate);
    }

    private void iterateVideoImaDelegates(OnVideoImaDelegate onVideoImaDelegate) {
        for (VideoImaDelegate videoImaDelegate : videoImaDelegateStorage) {
            onVideoImaDelegate.closure(videoImaDelegate);
        }
    }

}


