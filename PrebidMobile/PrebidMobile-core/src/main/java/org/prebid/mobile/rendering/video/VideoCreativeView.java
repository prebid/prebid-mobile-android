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

package org.prebid.mobile.rendering.video;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.models.ViewPool;
import org.prebid.mobile.rendering.utils.helpers.Dips;
import org.prebid.mobile.rendering.utils.helpers.InsetsUtils;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.utils.url.action.BrowserAction;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkAction;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkPlusAction;
import org.prebid.mobile.rendering.utils.url.action.UrlAction;
import org.prebid.mobile.rendering.views.VolumeControlView;

import static android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
import static android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT;

public class VideoCreativeView extends RelativeLayout {

    private static final String TAG = VideoCreativeView.class.getSimpleName();

    private final VideoCreativeViewListener videoCreativeViewListener;

    @Nullable private View callToActionView;
    private ExoPlayerView exoPlayerView;
    private VolumeControlView volumeControlView;

    private String callToActionUrl;
    private boolean urlHandleInProgress;
    private int broadcastId;
    private boolean isFirstRunOfCreative = true;
    private boolean isMuted = false;

    public VideoCreativeView(
        Context context,
        VideoCreativeViewListener videoCreativeViewListener

    ) throws AdException {
        super(context);
        this.videoCreativeViewListener = videoCreativeViewListener;
        init();
    }

    public void setVideoUri(Uri videoUri) {
        if (videoUri == null) {
            LogUtil.error(TAG, "setVideoUri: Failed. Provided uri is null.");
            return;
        }

        exoPlayerView.setVideoUri(videoUri);
    }

    public void setVastVideoDuration(long duration) {
        exoPlayerView.setVastVideoDuration(duration);
    }

    public void setBroadcastId(int broadcastId) {
        this.broadcastId = broadcastId;
    }

    public void setCallToActionUrl(String callToActionUrl) {
        this.callToActionUrl = callToActionUrl;
    }

    public String getCallToActionUrl() {
        return callToActionUrl;
    }

    public void start(float initialVolume) {
        exoPlayerView.start(initialVolume);
    }

    public void stop() {
        exoPlayerView.stop();
    }

    public void pause() {
        exoPlayerView.pause();
    }

    public void resume() {
        exoPlayerView.resume();
    }

    public void setStartIsMutedProperty(boolean isMuted) {
        if (isFirstRunOfCreative) {
            isFirstRunOfCreative = false;
            if (isMuted) {
                mute();
            } else {
                unMute();
            }
        }
    }

    public void mute() {
        isMuted = true;
        exoPlayerView.mute();
        updateVolumeControlView(VolumeControlView.VolumeState.MUTED);
    }

    public void unMute() {
        isMuted = false;
        exoPlayerView.unMute();
        updateVolumeControlView(VolumeControlView.VolumeState.UN_MUTED);
    }

    public void showCallToAction() {
        addCallToActionView();
    }

    public void hideCallToAction() {
        if (callToActionView != null) {
            removeView(callToActionView);
            callToActionView = null;
        }
    }

    public void showVolumeControls() {
        addVolumeControlView();
    }

    public VolumeControlView getVolumeControlView() {
        return volumeControlView;
    }

    public void hideVolumeControls() {
        if (volumeControlView != null) {
            removeView(volumeControlView);
            volumeControlView = null;
        }
    }

    public void enableVideoPlayerClick() {
        setOnClickListener(view -> handleCallToActionClick());
    }

    public boolean isPlaying() {
        return exoPlayerView.isPlaying();
    }

    public boolean hasVideoStarted() {
        return exoPlayerView.getCurrentPosition() != -1;
    }

    public VideoPlayerView getVideoPlayerView() {
        return exoPlayerView;
    }

    public float getVolume() {
        return exoPlayerView.getVolume();
    }

    public void destroy() {
        exoPlayerView.destroy();
        ViewPool.getInstance().clear();
    }

    private void init() throws AdException {
        LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        setLayoutParams(layoutParams);

        exoPlayerView = (ExoPlayerView) ViewPool.getInstance()
                                                .getUnoccupiedView(getContext(),
                                                        videoCreativeViewListener,
                                                        AdFormat.VAST,
                                                        null
                                                );

        addView(exoPlayerView);
    }

    private void addCallToActionView() {
        callToActionView = inflate(getContext(), R.layout.lyt_call_to_action, null);
        callToActionView.setOnClickListener(v -> handleCallToActionClick());

        final int width = Dips.dipsToIntPixels(128, getContext());
        final int height = Dips.dipsToIntPixels(36, getContext());
        final int margin = Dips.dipsToIntPixels(25, getContext());

        LayoutParams layoutParams = new LayoutParams(width, height);

        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(margin, margin, margin, margin);

        addView(callToActionView, layoutParams);
        InsetsUtils.addCutoutAndNavigationInsets(callToActionView);
    }

    private void addVolumeControlView() {
        boolean notContainsVolumeControl = indexOfChild(volumeControlView) == -1;
        if (notContainsVolumeControl) {
            LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            volumeControlView = new VolumeControlView(getContext(), isMuted ? VolumeControlView.VolumeState.MUTED : VolumeControlView.VolumeState.UN_MUTED);
            volumeControlView.setVolumeControlListener(state -> {
                if (state == VolumeControlView.VolumeState.MUTED) {
                    mute();
                } else {
                    unMute();
                }
            });

            final int margin = Dips.dipsToIntPixels(10, getContext());

            layoutParams.addRule(ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(ALIGN_PARENT_LEFT);
            layoutParams.setMargins(margin, margin, margin, margin);

            addView(volumeControlView, layoutParams);
        }
    }

    private void handleCallToActionClick() {
        if (urlHandleInProgress) {
            LogUtil.debug(TAG, "handleCallToActionClick: Skipping. Url handle in progress");
            return;
        }
        urlHandleInProgress = true;

        createUrlHandler().handleUrl(getContext(), callToActionUrl, null, true);

        videoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_CLICK);
    }

    private void updateVolumeControlView(VolumeControlView.VolumeState unMuted) {
        if (volumeControlView != null) {
            volumeControlView.updateIcon(unMuted);
        }
    }

    UrlHandler createUrlHandler() {
        return new UrlHandler.Builder().withDeepLinkPlusAction(new DeepLinkPlusAction())
                                       .withDeepLinkAction(new DeepLinkAction())
                                       .withBrowserAction(new BrowserAction(broadcastId, null))
            .withResultListener(new UrlHandler.UrlHandlerResultListener() {
                @Override
                public void onSuccess(String url, UrlAction urlAction) {
                    urlHandleInProgress = false;
                }

                @Override
                public void onFailure(String url) {
                    urlHandleInProgress = false;
                    LogUtil.debug(TAG, "Failed to handleUrl: " + url + ". Handling fallback");
                }
            })
            .build();
    }
}
