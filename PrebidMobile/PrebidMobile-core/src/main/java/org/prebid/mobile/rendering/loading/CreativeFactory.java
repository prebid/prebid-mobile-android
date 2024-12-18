/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.loading;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.listeners.CreativeResolutionListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.TrackingEvent;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.RewardedVideoCreative;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.video.vast.VASTErrorCodes;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CreativeFactory {

    private static final String TAG = CreativeFactory.class.getSimpleName();

    private AbstractCreative creative;
    private CreativeModel creativeModel;

    private WeakReference<Context> contextReference;
    private Listener listener;

    private OmAdSessionManager omAdSessionManager;
    private final InterstitialManager interstitialManager;
    private TimeoutState timeoutState = TimeoutState.PENDING;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());

    public CreativeFactory(
            Context context,
            CreativeModel creativeModel,
            Listener listener,
            OmAdSessionManager omAdSessionManager,
            InterstitialManager interstitialManager
    ) throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }

        if (creativeModel == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "CreativeModel is null");
        }

        if (listener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "CreativeFactory listener is null");
        }

        this.listener = listener;
        contextReference = new WeakReference<>(context);
        this.creativeModel = creativeModel;
        this.omAdSessionManager = omAdSessionManager;
        this.interstitialManager = interstitialManager;
    }

    public void start() {
        try {
            AdUnitConfiguration configuration = creativeModel.getAdConfiguration();

            if (configuration.isAdType(AdFormat.BANNER) || configuration.isAdType(AdFormat.INTERSTITIAL)) {
                attemptAuidCreative();
            } else if (configuration.isAdType(AdFormat.VAST)) {
                attemptVastCreative();
            } else {
                String msg = "Unable to start creativeFactory. adConfig.adUnitIdentifierType doesn't match supported types adConfig.adFormat: " + configuration.getAdFormats();
                LogUtil.error(TAG, msg);
                AdException adException = new AdException(AdException.INTERNAL_ERROR, msg);
                listener.onFailure(adException);
            }
        } catch (Exception exception) {
            String message = "Creative Factory failed: " + exception.getMessage();
            LogUtil.error(TAG, message + Log.getStackTraceString(exception));
            AdException adException = new AdException(AdException.INTERNAL_ERROR, message);
            listener.onFailure(adException);
        }
    }

    public void destroy() {
        if (creative != null) {
            creative.destroy();
        }
        timeoutHandler.removeCallbacks(null);
    }

    public AbstractCreative getCreative() {
        return creative;
    }

    private void attemptAuidCreative() throws Exception {

        creative = new HTMLCreative(contextReference.get(), creativeModel, omAdSessionManager, interstitialManager);
        creative.setResolutionListener(new CreativeFactoryCreativeResolutionListener(this));

        ArrayList<String> impressionUrls = new ArrayList<>();
        String viewableUrl = creativeModel.getViewableUrl();
        if (Utils.isNotBlank(viewableUrl)) {
            impressionUrls.add(viewableUrl);
        }
        String impressionUrl = creativeModel.getImpressionUrl();
        if (Utils.isNotBlank(impressionUrl)) {
            impressionUrls.add(impressionUrl);
        }


        if (creativeModel.isRequireImpressionUrl() && impressionUrls.isEmpty()) {
            listener.onFailure(new AdException(AdException.INTERNAL_ERROR, "Tracking info not found"));
        } else {
            creativeModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, impressionUrls);

            ArrayList<String> clickUrls = new ArrayList<>();
            if (Utils.isNotBlank(creativeModel.getClickUrl())) {
                clickUrls.add(creativeModel.getClickUrl());
            }
            creativeModel.registerTrackingEvent(TrackingEvent.Events.CLICK, clickUrls);
        }

        long creativeDownloadTimeout = PrebidMobile.getCreativeFactoryTimeout();
        if (creativeModel.getAdConfiguration().isAdType(AdFormat.INTERSTITIAL)) {
            creativeDownloadTimeout = PrebidMobile.getCreativeFactoryTimeoutPreRenderContent();
        }
        markWorkStart(creativeDownloadTimeout);
        creative.load();
    }

    private void attemptVastCreative() {
        VideoCreativeModel videoCreativeModel = (VideoCreativeModel) creativeModel;
        String mediaUrl = videoCreativeModel.getMediaUrl();
        if (Utils.isBlank(mediaUrl) || mediaUrl.equals("invalid media file")) {
            listener.onFailure(new AdException(
                AdException.INTERNAL_ERROR,
                VASTErrorCodes.NO_SUPPORTED_MEDIA_ERROR.toString()
            ));
            return;
        }

        //get the tracking url for all event types & do the registration here.
        for (VideoAdEvent.Event videoEvent : VideoAdEvent.Event.values()) {
            videoCreativeModel.registerVideoEvent(videoEvent, videoCreativeModel.getVideoEventUrls().get(videoEvent));
        }
        ArrayList<String> impressions = new ArrayList<>(2);
        impressions.add(creativeModel.getImpressionUrl());
        impressions.add(creativeModel.getViewableUrl());
        videoCreativeModel.registerTrackingEvent(
            TrackingEvent.Events.IMPRESSION,
            impressions
        );

        VideoCreative newCreative;
        try {
            if (creativeModel.getAdConfiguration().isRewarded()) {
                newCreative = new RewardedVideoCreative(
                    contextReference.get(),
                    videoCreativeModel,
                    omAdSessionManager,
                    interstitialManager
                );
            } else {
                newCreative = new VideoCreative(contextReference.get(),
                        videoCreativeModel,
                        omAdSessionManager,
                        interstitialManager
                );
            }

            newCreative.setResolutionListener(new CreativeFactoryCreativeResolutionListener(this));
            creative = newCreative;
            markWorkStart(PrebidMobile.getCreativeFactoryTimeoutPreRenderContent());
            newCreative.load();
        } catch (Exception exception) {
            LogUtil.error(TAG, "VideoCreative creation failed: " + Log.getStackTraceString(exception));
            listener.onFailure(new AdException(
                    AdException.INTERNAL_ERROR,
                    "VideoCreative creation failed: " + exception.getMessage()
            ));
        }
    }

    private void markWorkStart(long timeout) {
        timeoutState = TimeoutState.RUNNING;
        timeoutHandler.postDelayed(() -> {
            if (timeoutState != TimeoutState.FINISHED) {
                timeoutState = TimeoutState.EXPIRED;
                listener.onFailure((new AdException(AdException.INTERNAL_ERROR, "Creative factory Timeout")));
            }
        }, timeout);
    }

    /**
     * Listens for when Creatives are made
     * Relays that back to CreativeFactory's listener
     */
    public interface Listener {

        void onSuccess();

        void onFailure(AdException exception);
    }

    public enum TimeoutState {
        PENDING,
        RUNNING,
        FINISHED,
        EXPIRED
    }

    static class CreativeFactoryCreativeResolutionListener implements CreativeResolutionListener {

        private WeakReference<CreativeFactory> weakCreativeFactory;

        CreativeFactoryCreativeResolutionListener(CreativeFactory creativeFactory) {
            weakCreativeFactory = new WeakReference<>(creativeFactory);
        }

        @Override
        public void creativeReady(AbstractCreative creative) {
            CreativeFactory creativeFactory = weakCreativeFactory.get();
            if (creativeFactory == null) {
                LogUtil.warning(TAG, "CreativeFactory is null");
                return;
            }
            if (creativeFactory.timeoutState == TimeoutState.EXPIRED) {
                creativeFactory.listener.onFailure(new AdException(AdException.INTERNAL_ERROR, "Creative Timeout"));
                LogUtil.warning(TAG, "Creative timed out, backing out");
                return;
            }
            creativeFactory.timeoutState = TimeoutState.FINISHED;

            creativeFactory.listener.onSuccess();
        }

        @Override
        public void creativeFailed(AdException error) {
            CreativeFactory creativeFactory = weakCreativeFactory.get();
            if (creativeFactory == null) {
                LogUtil.warning(TAG, "CreativeFactory is null");
                return;
            }

            creativeFactory.timeoutHandler.removeCallbacks(null);

            creativeFactory.listener.onFailure(error);
        }
    }
}
