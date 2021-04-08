package org.prebid.mobile.rendering.views.video;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.listeners.VideoDialogListener;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

public class VideoDialog extends AdBaseDialog {
    private static final String TAG = "VideoDialog";

    private final AdViewManager mAdViewManager;

    private VideoCreativeView mAdView;
    private VideoDialogListener mVideoDialogListener;

    public VideoDialog(Context context,
                       VideoCreativeView videoCreativeView,
                       AdViewManager adViewManager,
                       InterstitialManager interstitialManager,
                       FrameLayout adViewContainer) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen, interstitialManager);
        mAdViewContainer = adViewContainer;
        mAdViewManager = adViewManager;
        mAdViewContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                      ViewGroup.LayoutParams.MATCH_PARENT));
        mAdView = videoCreativeView;
        setContentView(mAdViewContainer);

        initListeners();
    }

    public void setVideoDialogListener(VideoDialogListener videoDialogListener) {
        mVideoDialogListener = videoDialogListener;
    }

    @Override
    protected void handleCloseClick() {
        dismiss();
        unApplyOrientation();

        if (mVideoDialogListener != null) {
            mVideoDialogListener.onVideoDialogClosed();
        }
    }

    @Override
    protected void handleDialogShow() {
        handleShowAction();
    }

    public void showBannerCreative(View creativeView) {
        mAdViewContainer.removeAllViews();
        creativeView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                  ViewGroup.LayoutParams.MATCH_PARENT));
        lockOrientation();
        mAdViewContainer.addView(creativeView);
        mDisplayView = creativeView;
        addCloseView();
        mAdView = null;
    }

    private void initListeners() {
        setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
    }

    private void handleShowAction() {
        mAdViewManager.trackVideoStateChange(InternalPlayerState.EXPANDED);
        mAdView.unMute();

        changeCloseViewVisibility(View.VISIBLE);
        mAdView.showCallToAction();

        mAdViewManager.updateAdView(mAdViewContainer);
    }
}
