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

package org.prebid.mobile.rendering.mraid.methods;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.*;

public class MraidController {
    private static final String TAG = MraidController.class.getSimpleName();

    protected InterstitialManager mInterstitialManager;
    private MraidUrlHandler mMraidUrlHandler;
    private MraidResize mMraidResize;
    private MraidStorePicture mMraidStorePicture;
    private MraidCalendarEvent mMraidCalendarEvent;
    private MraidExpand mMraidExpand;

    private InterstitialManagerMraidDelegate mInterstitialManagerMraidDelegate = new InterstitialManagerMraidDelegate() {
        @Override
        public boolean collapseMraid() {
            if (mMraidExpand == null) {
                return false;
            }
            if (mMraidExpand.isMraidExpanded()) {
                mInterstitialManager.getHtmlCreative().mraidAdCollapsed();
            }
            mMraidExpand.nullifyDialog();
            //make MraidExpand null, so a new MraidExpand is created for a new expansion
            mMraidExpand = null;
            return true;
        }

        @Override
        public void closeThroughJs(WebViewBase viewToClose) {
            MraidController.this.closeThroughJs(viewToClose);
        }

        @Override
        public void displayPrebidWebViewForMraid(WebViewBase adBaseView, boolean isNewlyLoaded, MraidEvent mraidEvent) {
            MraidController.this.displaPrebidWebViewForMraid(adBaseView, isNewlyLoaded, mraidEvent);
        }

        @Override
        public void displayViewInInterstitial(View adBaseView, boolean addOldViewToBackStack, MraidEvent expandUrl, DisplayCompletionListener displayCompletionListener) {
            displayMraidInInterstitial(adBaseView, addOldViewToBackStack, expandUrl, displayCompletionListener);
        }

        @Override
        public void destroyMraidExpand() {
            if (mMraidExpand != null) {
                mMraidExpand.destroy();
                mMraidExpand = null;
            }
        }
    };

    public MraidController(
        @NonNull
            InterstitialManager interstitialManager) {
        mInterstitialManager = interstitialManager;
        mInterstitialManager.setMraidDelegate(mInterstitialManagerMraidDelegate);
    }

    public void close(WebViewBase oldWebViewBase) {
        mInterstitialManager.interstitialClosed(oldWebViewBase);
    }

    public void createCalendarEvent(BaseJSInterface jsInterface, String parameters) {
        if (mMraidCalendarEvent == null) {
            mMraidCalendarEvent = new MraidCalendarEvent(jsInterface);
        }
        mMraidCalendarEvent.createCalendarEvent(parameters);
    }

    public void expand(WebViewBase oldWebViewBase, PrebidWebViewBase twoPartNewWebViewBase,
                       MraidEvent mraidEvent) {
        oldWebViewBase.getMraidListener().loadMraidExpandProperties();
        if (TextUtils.isEmpty(mraidEvent.mraidActionHelper)) {
            //create an mraidExpand & call expand on it to open up a dialog
            displaPrebidWebViewForMraid(oldWebViewBase, false, mraidEvent);
        }
        else {
            //2 part
            twoPartNewWebViewBase.getMraidWebView().setMraidEvent(mraidEvent);
        }
    }

    public void handleMraidEvent(MraidEvent event, HTMLCreative creative,
                                 WebViewBase oldWebViewBase, PrebidWebViewBase twoPartNewWebViewBase) {
        switch (event.mraidAction) {
            case ACTION_EXPAND:
                if (Utils.isBlank(event.mraidActionHelper)) {
                    LogUtil.debug(TAG, "One part expand");
                    expand(oldWebViewBase, twoPartNewWebViewBase, event);
                }
                else {
                    //2 part : new webview
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new TwoPartExpandRunnable(creative, event, oldWebViewBase, this));
                }
                break;

            case ACTION_CLOSE:
                close(oldWebViewBase);
                break;

            case ACTION_PLAY_VIDEO:
                playVideo(oldWebViewBase, event);
                break;

            case ACTION_OPEN:
                final AdUnitConfiguration adConfiguration = creative.getCreativeModel().getAdConfiguration();
                open(oldWebViewBase, event.mraidActionHelper, adConfiguration.getBroadcastId());
                break;

            case ACTION_STORE_PICTURE:
                storePicture(oldWebViewBase, event.mraidActionHelper);
                break;

            case ACTION_CREATE_CALENDAR_EVENT:
                createCalendarEvent(oldWebViewBase.getMRAIDInterface(), event.mraidActionHelper);
                break;

            case ACTION_ORIENTATION_CHANGE:
                changeOrientation();
                break;

            case ACTION_RESIZE:
                resize(oldWebViewBase);
                break;
            case ACTION_UNLOAD:
                unload(creative, oldWebViewBase);
                break;
        }
    }

    public void changeOrientation() {
        if (mMraidExpand != null && mMraidExpand.getInterstitialViewController() != null) {
            try {
                mMraidExpand.getInterstitialViewController().handleSetOrientationProperties();
            }
            catch (AdException e) {
                LogUtil.error(TAG, Log.getStackTraceString(e));
            }
        }
    }

    public void open(WebViewBase oldWebViewBase, String uri, int broadcastId) {
        if (mMraidUrlHandler == null) {
            mMraidUrlHandler = new MraidUrlHandler(oldWebViewBase.getContext(),
                                                   oldWebViewBase.getMRAIDInterface());
        }
        mMraidUrlHandler.open(uri, broadcastId);
    }

    public void playVideo(WebViewBase oldWebViewBase, MraidEvent event) {
        displayVideoURLwithMPPlayer(oldWebViewBase, event);
    }

    public void resize(WebViewBase oldWebViewBase) {
        if (mMraidResize == null) {
            mMraidResize = new MraidResize(oldWebViewBase.getContext(),
                                           oldWebViewBase.getMRAIDInterface(),
                                           oldWebViewBase,
                                           mInterstitialManager);
        }
        mMraidResize.resize();
    }

    public void storePicture(WebViewBase oldWebViewBase, String uri) {
        if (mMraidStorePicture == null) {
            mMraidStorePicture = new MraidStorePicture(oldWebViewBase.getContext(),
                                                       oldWebViewBase.getMRAIDInterface(),
                                                       oldWebViewBase);
        }
        mMraidStorePicture.storePicture(uri);
    }

    public void destroy() {
        if (mMraidResize != null) {
            mMraidResize.destroy();
            mMraidResize = null;
        }
        if (mMraidUrlHandler != null) {
            mMraidUrlHandler.destroy();
            mMraidUrlHandler = null;
        }

        if (mMraidExpand != null) {
            mMraidExpand.destroy();
            mMraidExpand = null;
        }
    }

    private void displayMraidInInterstitial(final View adBaseView,
                                            boolean addOldViewToBackStack,
                                            final MraidEvent mraidEvent,
                                            final DisplayCompletionListener displayCompletionListener) {
        if (mMraidExpand == null) {
            initMraidExpand(adBaseView, displayCompletionListener, mraidEvent);
            return;
        }
        //2 part may be?? OR click of video.close from an expanded ad
        if (addOldViewToBackStack) {
            mInterstitialManager.addOldViewToBackStack((WebViewBase) adBaseView,
                                                       mraidEvent.mraidActionHelper,
                                                       mMraidExpand.getInterstitialViewController());
        }

        mMraidExpand.setDisplayView(adBaseView);

        if (displayCompletionListener != null) {
            displayCompletionListener.onDisplayCompleted();
        }
    }

    private void displaPrebidWebViewForMraid(final WebViewBase adBaseView,
                                             final boolean isNewlyLoaded,
                                             MraidEvent mraidEvent) {
        displayMraidInInterstitial(adBaseView, false, mraidEvent, () -> {
            if (isNewlyLoaded) {
                //handle 2 part expand
                PrebidWebViewBase oxWebview = (PrebidWebViewBase) adBaseView.getPreloadedListener();
                oxWebview.initMraidExpanded();
            }
        });
    }

    private void displayVideoURLwithMPPlayer(WebViewBase adBaseView, final MraidEvent mraidEvent) {
        //open up an expanded dialog(always fullscreen) & then play video on videoview in that expanded dialog
        displayMraidInInterstitial(adBaseView, true, mraidEvent, () -> {
            MraidPlayVideo mraidPlayVideo = new MraidPlayVideo();
            mraidPlayVideo.playVideo(mraidEvent.mraidActionHelper);
        });
    }

    private void closeThroughJs(WebViewBase viewToClose) {
        MraidClose mraidClose = new MraidClose(viewToClose.getContext(), viewToClose.getMRAIDInterface(), viewToClose);
        mraidClose.closeThroughJS();
    }

    private void unload(HTMLCreative creative, WebViewBase webViewBase) {
        close(webViewBase);
        creative.getCreativeViewListener().creativeDidComplete(creative);
    }

    @VisibleForTesting
    protected void initMraidExpand(final View adBaseView,
                                   final DisplayCompletionListener displayCompletionListener,
                                   final MraidEvent mraidEvent) {
        mMraidExpand = new MraidExpand(adBaseView.getContext(), ((WebViewBase) adBaseView), mInterstitialManager);

        if (mraidEvent.mraidAction.equals(JSInterface.ACTION_EXPAND)) {
            mMraidExpand.setMraidExpanded(true);
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            try {
                LogUtil.debug(TAG, "mraidExpand");
                //send click event on expand
                ((WebViewBase) adBaseView).sendClickCallBack(mraidEvent.mraidActionHelper);
                mMraidExpand.expand(mraidEvent.mraidActionHelper, () -> {
                    if (displayCompletionListener != null) {
                        displayCompletionListener.onDisplayCompleted();

                        //send expandedCallback to pubs
                        mInterstitialManager.getHtmlCreative().mraidAdExpanded();
                    }
                });
            }
            catch (Exception e) {
                LogUtil.error(TAG, "mraidExpand failed at displayViewInInterstitial: " + Log.getStackTraceString(e));
            }
        });
    }

    public interface DisplayCompletionListener {
        void onDisplayCompleted();
    }
}
