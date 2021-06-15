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

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.CreativeResolutionListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.TrackingEvent;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
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
    private static final long BANNER_TIMEOUT = 6 * 1000;
    private static final long VAST_TIMEOUT = 30 * 1000;

    private AbstractCreative mCreative;
    private CreativeModel mCreativeModel;

    private WeakReference<Context> mContextReference;
    private Listener mListener;

    private OmAdSessionManager mOmAdSessionManager;
    private final InterstitialManager mInterstitialManager;
    private TimeoutState mTimeoutState = TimeoutState.PENDING;
    private Handler mTimeoutHandler = new Handler(Looper.getMainLooper());

    public CreativeFactory(Context context,
                           CreativeModel creativeModel,
                           Listener listener,
                           OmAdSessionManager omAdSessionManager,
                           InterstitialManager interstitialManager)
    throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }

        if (creativeModel == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "CreativeModel is null");
        }

        if (listener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "CreativeFactory listener is null");
        }

        mListener = listener;
        mContextReference = new WeakReference<>(context);
        mCreativeModel = creativeModel;
        mOmAdSessionManager = omAdSessionManager;
        mInterstitialManager = interstitialManager;
    }

    public void start() {
        try {
            AdConfiguration.AdUnitIdentifierType adUnitIdentifierType = mCreativeModel.getAdConfiguration().getAdUnitIdentifierType();
            switch (adUnitIdentifierType) {
                case BANNER:
                case INTERSTITIAL:
                    attemptAuidCreative();
                    break;
                case VAST:
                    attemptVastCreative();
                    break;
                default:
                    String msg = "Unable to start creativeFactory. adConfig.adUnitIdentifierType doesn't match supported types"
                                 + "adConfig.adUnitIdentifierType: " + adUnitIdentifierType;
                    LogUtil.error(TAG, msg);
                    AdException adException = new AdException(AdException.INTERNAL_ERROR, msg);
                    mListener.onFailure(adException);
                    break;
            }
        }
        catch (Exception exception) {
            String message = "Creative Factory failed: " + exception.getMessage();
            LogUtil.error(TAG, message + Log.getStackTraceString(exception));
            AdException adException = new AdException(AdException.INTERNAL_ERROR, message);
            mListener.onFailure(adException);
        }
    }

    public void destroy() {
        if (mCreative != null) {
            mCreative.destroy();
        }
        mTimeoutHandler.removeCallbacks(null);
    }

    public AbstractCreative getCreative() {
        return mCreative;
    }

    private void attemptAuidCreative() throws Exception {

        mCreative = new HTMLCreative(mContextReference.get(), mCreativeModel, mOmAdSessionManager, mInterstitialManager);
        mCreative.setResolutionListener(new CreativeFactoryCreativeResolutionListener(this));

        ArrayList<String> riUrls = new ArrayList<>();
        ArrayList<String> rcUrls = new ArrayList<>();

        //get the tracking url & do the registration here. add in the tracking stuff here
        //This needs to be more generalized and allow for multiple click urls
        if (!mCreativeModel.isRequireImpressionUrl() || Utils.isNotBlank(mCreativeModel.getImpressionUrl())) {
            if (!TextUtils.isEmpty(mCreativeModel.getImpressionUrl())) {
                riUrls.add(mCreativeModel.getImpressionUrl());
                mCreativeModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, riUrls);
            }
            //
            if (!TextUtils.isEmpty(mCreativeModel.getClickUrl())) {
                rcUrls.add(mCreativeModel.getClickUrl());
                mCreativeModel.registerTrackingEvent(TrackingEvent.Events.CLICK, rcUrls);
            }
        }
        else {
            mListener.onFailure(new AdException(AdException.INTERNAL_ERROR, "Tracking info not found"));
        }
        markWorkStart(BANNER_TIMEOUT);
        mCreative.load();
    }

    private void attemptVastCreative() {
        VideoCreativeModel videoCreativeModel = (VideoCreativeModel) mCreativeModel;
        String mediaUrl = videoCreativeModel.getMediaUrl();
        if (Utils.isBlank(mediaUrl) || mediaUrl.equals("invalid media file")) {
            mListener.onFailure(new AdException(AdException.INTERNAL_ERROR, VASTErrorCodes.NO_SUPPORTED_MEDIA_ERROR.toString()));
            return;
        }

        //get the tracking url for all event types & do the registration here.
        for (VideoAdEvent.Event videoEvent : VideoAdEvent.Event.values()) {
            videoCreativeModel.registerVideoEvent(videoEvent, videoCreativeModel.getVideoEventUrls().get(videoEvent));
        }

        VideoCreative newCreative;
        try {
            if (mCreativeModel.getAdConfiguration().isRewarded()) {
                newCreative = new RewardedVideoCreative(mContextReference.get(), videoCreativeModel, mOmAdSessionManager, mInterstitialManager);
            }
            else {
                newCreative = new VideoCreative(mContextReference.get(), videoCreativeModel, mOmAdSessionManager, mInterstitialManager);
            }

            newCreative.setResolutionListener(new CreativeFactoryCreativeResolutionListener(this));
            mCreative = newCreative;
            markWorkStart(VAST_TIMEOUT);
            newCreative.load();
        }
        catch (Exception exception) {
            LogUtil.error(TAG, "VideoCreative creation failed: " + Log.getStackTraceString(exception));
            mListener.onFailure(new AdException(AdException.INTERNAL_ERROR, "VideoCreative creation failed: " + exception.getMessage()));
        }
    }

    private void markWorkStart(long timeout) {
        mTimeoutState = TimeoutState.RUNNING;
        mTimeoutHandler.postDelayed(() -> {
            if (mTimeoutState != TimeoutState.FINISHED) {
                mTimeoutState = TimeoutState.EXPIRED;
                mListener.onFailure((new AdException(AdException.INTERNAL_ERROR, "Creative factory Timeout")));
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

        private WeakReference<CreativeFactory> mWeakCreativeFactory;

        CreativeFactoryCreativeResolutionListener(CreativeFactory creativeFactory) {
            mWeakCreativeFactory = new WeakReference<>(creativeFactory);
        }

        @Override
        public void creativeReady(AbstractCreative creative) {
            CreativeFactory creativeFactory = mWeakCreativeFactory.get();
            if (creativeFactory == null) {
                LogUtil.warn(TAG, "CreativeFactory is null");
                return;
            }
            if (creativeFactory.mTimeoutState == TimeoutState.EXPIRED) {
                creativeFactory.mListener.onFailure(new AdException(AdException.INTERNAL_ERROR, "Creative Timeout"));
                LogUtil.warn(TAG, "Creative timed out, backing out");
                return;
            }
            creativeFactory.mTimeoutState = TimeoutState.FINISHED;

            creativeFactory.mListener.onSuccess();
        }

        @Override
        public void creativeFailed(AdException error) {
            CreativeFactory creativeFactory = mWeakCreativeFactory.get();
            if (creativeFactory == null) {
                LogUtil.warn(TAG, "CreativeFactory is null");
                return;
            }

            creativeFactory.mTimeoutHandler.removeCallbacks(null);

            creativeFactory.mListener.onFailure(error);
        }
    }
}
