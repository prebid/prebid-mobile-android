package com.openx.apollo.video;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.openx.apollo.R;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.listeners.VideoCreativeViewListener;
import com.openx.apollo.models.ViewPool;
import com.openx.apollo.utils.helpers.Dips;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.utils.url.UrlHandler;
import com.openx.apollo.utils.url.action.BrowserAction;
import com.openx.apollo.utils.url.action.DeepLinkAction;
import com.openx.apollo.utils.url.action.DeepLinkPlusAction;
import com.openx.apollo.utils.url.action.UrlAction;
import com.openx.apollo.views.VolumeControlView;

import static android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
import static android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT;
import static com.openx.apollo.models.AdConfiguration.AdUnitIdentifierType.VAST;

public class VideoCreativeView extends RelativeLayout {
    private static final String TAG = VideoCreativeView.class.getSimpleName();

    private final VideoCreativeViewListener mVideoCreativeViewListener;

    @Nullable
    private View mCallToActionView;
    private ExoPlayerView mExoPlayerView;
    private VolumeControlView mVolumeControlView;

    private String mCallToActionUrl;
    private boolean mUrlHandleInProgress;
    private int mBroadcastId;

    public VideoCreativeView(Context context, VideoCreativeViewListener videoCreativeViewListener)
    throws AdException {
        super(context);
        mVideoCreativeViewListener = videoCreativeViewListener;
        init();
    }

    public void setVideoUri(Uri videoUri) {
        if (videoUri == null) {
            OXLog.error(TAG, "setVideoUri: Failed. Provided uri is null.");
            return;
        }

        mExoPlayerView.setVideoUri(videoUri);
    }

    public void setVastVideoDuration(long duration) {
        mExoPlayerView.setVastVideoDuration(duration);
    }

    public void setBroadcastId(int broadcastId) {
        mBroadcastId = broadcastId;
    }

    public void setCallToActionUrl(String callToActionUrl) {
        mCallToActionUrl = callToActionUrl;
    }

    public String getCallToActionUrl() {
        return mCallToActionUrl;
    }

    public void start(float initialVolume) {
        mExoPlayerView.start(initialVolume);
    }

    public void stop() {
        mExoPlayerView.stop();
    }

    public void pause() {
        mExoPlayerView.pause();
    }

    public void resume() {
        mExoPlayerView.resume();
    }

    public void mute() {
        mExoPlayerView.mute();

        updateVolumeControlView(VolumeControlView.VolumeState.MUTED);
    }

    public void unMute() {
        mExoPlayerView.unMute();

        updateVolumeControlView(VolumeControlView.VolumeState.UN_MUTED);
    }

    public void showCallToAction() {
        addCallToActionView();
    }

    public void hideCallToAction() {
        if (mCallToActionView != null) {
            removeView(mCallToActionView);
            mCallToActionView = null;
        }
    }

    public void showVolumeControls() {
        addVolumeControlView();
    }

    public void hideVolumeControls() {
        if (mVolumeControlView != null) {
            removeView(mVolumeControlView);
            mVolumeControlView = null;
        }
    }

    public void enableVideoPlayerClick() {
        setOnClickListener(view -> handleCallToActionClick());
    }

    public boolean isPlaying() {
        return mExoPlayerView.isPlaying();
    }

    public boolean hasVideoStarted() {
        return mExoPlayerView.getCurrentPosition() != -1;
    }

    public VideoPlayerView getVideoPlayerView() {
        return mExoPlayerView;
    }

    public float getVolume() {
        return mExoPlayerView.getVolume();
    }

    public void destroy() {
        mExoPlayerView.destroy();
        ViewPool.getInstance().clear();
    }

    private void init() throws AdException {
        LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        setLayoutParams(layoutParams);

        mExoPlayerView = (ExoPlayerView) ViewPool
            .getInstance()
            .getUnoccupiedView(getContext(), mVideoCreativeViewListener, VAST, null);

        addView(mExoPlayerView);
    }

    private void addCallToActionView() {
        mCallToActionView = inflate(getContext(), R.layout.lyt_call_to_action, null);
        mCallToActionView.setOnClickListener(v -> handleCallToActionClick());

        final int width = Dips.dipsToIntPixels(128, getContext());
        final int height = Dips.dipsToIntPixels(36, getContext());
        final int margin = Dips.dipsToIntPixels(25, getContext());

        LayoutParams layoutParams = new LayoutParams(width, height);

        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(margin, margin, margin, margin);

        addView(mCallToActionView, layoutParams);
    }

    private void addVolumeControlView() {
        LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        mVolumeControlView = new VolumeControlView(getContext(), VolumeControlView.VolumeState.MUTED);
        mVolumeControlView.setVolumeControlListener(state -> {
            if (state == VolumeControlView.VolumeState.MUTED) {
                mute();
            }
            else {
                unMute();
            }
        });

        final int margin = Dips.dipsToIntPixels(10, getContext());

        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(ALIGN_PARENT_LEFT);
        layoutParams.setMargins(margin, margin, margin, margin);

        addView(mVolumeControlView, layoutParams);
    }

    private void handleCallToActionClick() {
        if (mUrlHandleInProgress) {
            OXLog.debug(TAG, "handleCallToActionClick: Skipping. Url handle in progress");
            return;
        }
        mUrlHandleInProgress = true;

        createUrlHandler().handleUrl(getContext(), mCallToActionUrl, null, true);

        mVideoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_CLICK);
    }

    private void updateVolumeControlView(VolumeControlView.VolumeState unMuted) {
        if (mVolumeControlView != null) {
            mVolumeControlView.updateIcon(unMuted);
        }
    }

    UrlHandler createUrlHandler() {
        return new UrlHandler.Builder()
            .withDeepLinkPlusAction(new DeepLinkPlusAction())
            .withDeepLinkAction(new DeepLinkAction())
            .withBrowserAction(new BrowserAction(mBroadcastId, null))
            .withResultListener(new UrlHandler.UrlHandlerResultListener() {
                @Override
                public void onSuccess(String url, UrlAction urlAction) {
                    mUrlHandleInProgress = false;
                }

                @Override
                public void onFailure(String url) {
                    mUrlHandleInProgress = false;
                    OXLog.debug(TAG, "Failed to handleUrl: " + url + ". Handling fallback");
                }
            })
            .build();
    }
}
