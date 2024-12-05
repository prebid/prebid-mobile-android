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

    private final AdViewManager adViewManager;

    private VideoCreativeView adView;
    private VideoDialogListener videoDialogListener;

    public VideoDialog(
            Context context,
            VideoCreativeView videoCreativeView,
            AdViewManager adViewManager,
            InterstitialManager interstitialManager,
            FrameLayout adViewContainer
    ) {
        super(context, interstitialManager);
        this.adViewContainer = adViewContainer;
        this.adViewManager = adViewManager;
        this.adViewContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        adView = videoCreativeView;
        setContentView(this.adViewContainer);

        initListeners();
    }

    public void setVideoDialogListener(VideoDialogListener videoDialogListener) {
        this.videoDialogListener = videoDialogListener;
    }

    @Override
    protected void handleCloseClick() {
        dismiss();
        unApplyOrientation();

        if (videoDialogListener != null) {
            videoDialogListener.onVideoDialogClosed();
        }
    }

    @Override
    protected void handleDialogShow() {
        handleShowAction();
    }

    public void showBannerCreative(View creativeView) {
        adViewContainer.removeAllViews();
        creativeView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        lockOrientation();
        adViewContainer.addView(creativeView);
        displayView = creativeView;
        addCloseView();
        adView = null;
    }

    private void initListeners() {
        setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
    }

    private void handleShowAction() {
        adViewManager.trackVideoStateChange(InternalPlayerState.EXPANDED);
        adView.unMute();

        changeCloseViewVisibility(View.VISIBLE);
        adView.showCallToAction(true);

        adViewManager.updateAdView(adViewContainer);
    }
}
