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

package org.prebid.mobile.rendering.models;

import android.content.Context;
import android.view.View;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.CreativeResolutionListener;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public abstract class AbstractCreative {
    private static final String TAG = AbstractCreative.class.getSimpleName();

    protected WeakReference<Context> mContextReference;

    private CreativeModel mModel;
    private CreativeViewListener mCreativeViewListener;
    private CreativeResolutionListener mResolutionListener;

    protected WeakReference<OmAdSessionManager> mWeakOmAdSessionManager;

    protected InterstitialManager mInterstitialManager;

    private View mCreativeView;

    protected CreativeVisibilityTracker mCreativeVisibilityTracker;

    public AbstractCreative(Context context, CreativeModel model, OmAdSessionManager omAdSessionManager, InterstitialManager interstitialManager)
    throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }

        if (model == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "CreativeModel is null");
        }

        mContextReference = new WeakReference<>(context);

        mModel = model;

        mWeakOmAdSessionManager = new WeakReference<>(omAdSessionManager);
        mInterstitialManager = interstitialManager;

        mModel.registerActiveOmAdSession(omAdSessionManager);
    }

    public abstract boolean isDisplay();

    public abstract boolean isVideo();

    public abstract boolean isResolved();

    public abstract boolean isEndCard();

    /**
     * Subclasses should provide their implementation, default is printing a log statement
     */

    /**
     * Pause creative execution
     */
    public void pause() {
        LogUtil.debug(TAG, "pause(): Base method implementation: ignoring");
    }

    /**
     * Resume creative execution
     */
    public void resume() {
        LogUtil.debug(TAG, "resume(): Base method implementation: ignoring");
    }

    /**
     * UnMute creative
     */
    public void unmute() {
        LogUtil.debug(TAG, "unMute(): Base method implementation: ignoring");
    }

    /**
     * Mute creative
     */
    public void mute() {
        LogUtil.debug(TAG, "mute(): Base method implementation: ignoring");
    }

    /**
     * @return Whether the creative is playing
     */
    public boolean isPlaying() {
        LogUtil.debug(TAG, "isPlaying(): Returning default value: false");
        return false;
    }

    /**
     * Track video state change to OmEventTracker
     *
     * @param state to track
     */
    public void trackVideoStateChange(InternalPlayerState state) {
        LogUtil.debug(TAG, "trackVideoStateChange: Base method implementation: ignoring");
    }

    /**
     * @return if current creative is serving as interstitial that was closed
     */
    public boolean isInterstitialClosed() {
        LogUtil.debug(TAG, "isInterstitialClosed(): Returning default value: false");
        return false;
    }

    /**
     * @return media duration in ms
     */
    public long getMediaDuration() {
        LogUtil.debug(TAG, "getMediaDuration(): Returning default value: 0");
        return 0;
    }

    /**
     * @return video skip offset in ms
     */
    public long getVideoSkipOffset() {
        LogUtil.debug(TAG, "getVideoSkipOffset(): Returning default value: -1");
        return AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
    }

    /**
     * VideoAdEvent.Event to track
     */
    public void trackVideoEvent(VideoAdEvent.Event event) {
        LogUtil.debug(TAG, "trackVideoEvent(): Base method implementation: ignoring");
    }

    /**
     * @return if the current ad config suggests that this is a video
     */
    public boolean isBuiltInVideo() {
        return mModel.getAdConfiguration().isBuiltInVideo();
    }

    /**
     * Specific creative load.
     */
    public abstract void load() throws AdException;

    /**
     * Executed after processing transaction and creating OmAdSession in {@link AdViewManager}
     */
    public abstract void trackAdLoaded();

    /**
     * Specific creative display.
     */
    public abstract void display();

    /**
     * Create OM session for specific creative. Each creative must create appropriate OM AdSession (e.g. for HTML and Native)
     */
    public abstract void createOmAdSession();

    public abstract void startViewabilityTracker();

    /**
     * Specific creative cleanup. Creative must cleanup it's internal state.
     */
    public void destroy() {
        if (mCreativeVisibilityTracker != null) {
            mCreativeVisibilityTracker.stopVisibilityCheck();
            mCreativeVisibilityTracker = null;
        }
    }

    /**
     * Executed when window gains focus (e.g. when app is resumed from background)
     * Used by {@link AdViewManager} to handle refresh on view visibility change
     */
    public abstract void handleAdWindowFocus();

    /**
     * Executed when window loses focus (e.g. when app is going in background).
     * Used by {@link AdViewManager} to handle refresh on view visibility change
     */
    public abstract void handleAdWindowNoFocus();

    /**
     * Changes the {@link #mCreativeVisibilityTracker} state based on ad webView window focus.
     * If ad webView has no window focus - {@link #mCreativeVisibilityTracker} execution will be stopped.
     * If ad webView has window focus - {@link #mCreativeVisibilityTracker} execution will be restarted.
     *
     * @param adWebViewWindowFocus adWebView focus state
     */
    public void changeVisibilityTrackerState(boolean adWebViewWindowFocus) {
        if (mCreativeVisibilityTracker == null) {
            LogUtil.debug(TAG, "handleAdWebViewWindowFocusChange(): Failed. CreativeVisibilityTracker is null.");
            return;
        }

        if (!adWebViewWindowFocus) {
            mCreativeVisibilityTracker.stopVisibilityCheck();
        }
        else {
            mCreativeVisibilityTracker.stopVisibilityCheck();
            mCreativeVisibilityTracker.startVisibilityCheck(mContextReference.get());
        }
    }

    public void setResolutionListener(CreativeResolutionListener resolutionListener) {
        mResolutionListener = resolutionListener;
    }

    public CreativeResolutionListener getResolutionListener() {
        return mResolutionListener;
    }

    public void setCreativeViewListener(CreativeViewListener creativeViewListener) {
        mCreativeViewListener = creativeViewListener;
    }

    /**
     * Sets currently used creative view for specific creative.
     *
     * @param creativeView individual creative view. E.g. webView for HTMLCreative.
     */
    public void setCreativeView(View creativeView) {
        mCreativeView = creativeView;
    }

    /**
     * @return individual creative view. E.g. webView for HTMLCreative.
     */
    public View getCreativeView() {
        return mCreativeView;
    }

    /**
     * @return {@link CreativeModel} which can be used to track events or to access specific creative ad configuration.
     */
    @NonNull
    public CreativeModel getCreativeModel() {
        return mModel;
    }

    public void updateAdView(View view) {
        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            LogUtil.error(TAG, "Unable to updateAdView. OmAdSessionManager is null");
            return;
        }
        omAdSessionManager.registerAdView(view);
    }

    public CreativeViewListener getCreativeViewListener() {
        return mCreativeViewListener;
    }

    public void addOmFriendlyObstruction(InternalFriendlyObstruction friendlyObstruction) {
        if (friendlyObstruction == null) {
            LogUtil.debug(TAG, "addOmFriendlyObstruction: Obstruction view is null. Skip adding as friendlyObstruction");
            return;
        }

        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            LogUtil.error(TAG, "Unable to addOmFriendlyObstruction. OmAdSessionManager is null");
            return;
        }

        omAdSessionManager.addObstruction(friendlyObstruction);
    }

    protected void startOmSession(OmAdSessionManager omAdSessionManager, View view) {
        omAdSessionManager.registerAdView(view);
        omAdSessionManager.startAdSession();
    }
}
