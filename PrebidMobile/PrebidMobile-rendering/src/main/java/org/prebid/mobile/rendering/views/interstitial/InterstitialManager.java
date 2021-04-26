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

package org.prebid.mobile.rendering.views.interstitial;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.prebid.mobile.rendering.bidding.display.InterstitialView;
import org.prebid.mobile.rendering.bidding.display.VideoView;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.interstitial.AdInterstitialDialog;
import org.prebid.mobile.rendering.interstitial.InterstitialLayoutConfigurator;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerDisplayDelegate;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerInterface;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerVideoDelegate;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.mraid.methods.InterstitialManagerMraidDelegate;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.rendering.views.webview.WebViewBanner;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

import java.util.Stack;

import androidx.annotation.VisibleForTesting;

public class InterstitialManager implements InterstitialManagerInterface {

    private static String TAG = InterstitialManager.class.getSimpleName();
    private static final int INTERSTITIAL_WEBVIEW_ID = 123456789;

    private InterstitialDisplayPropertiesInternal mInterstitialDisplayProperties = new InterstitialDisplayPropertiesInternal();
    private AdInterstitialDialog mInterstitialDialog;

    private InterstitialManagerDisplayDelegate mInterstitialDisplayDelegate;
    private InterstitialManagerVideoDelegate mInterstitialVideoDelegate;

    private InterstitialManagerMraidDelegate mMraidDelegate;
    private AdViewManager.AdViewManagerInterstitialDelegate mAdViewManagerInterstitialDelegate;

    private final Stack<View> mViewStack = new Stack<>();

    public void setMraidDelegate(InterstitialManagerMraidDelegate mraidDelegate) {
        mMraidDelegate = mraidDelegate;
    }

    public void configureInterstitialProperties(AdConfiguration adConfiguration) {
        InterstitialLayoutConfigurator.configureDisplayProperties(adConfiguration, mInterstitialDisplayProperties);
    }

    public InterstitialDisplayPropertiesInternal getInterstitialDisplayProperties() {
        return mInterstitialDisplayProperties;
    }

    public void setInterstitialDisplayDelegate(InterstitialManagerDisplayDelegate interstitialDisplayDelegate) {
        mInterstitialDisplayDelegate = interstitialDisplayDelegate;
    }

    public void setInterstitialVideoDelegate(InterstitialManagerVideoDelegate interstitialVideoDelegate) {
        mInterstitialVideoDelegate = interstitialVideoDelegate;
    }

    @VisibleForTesting
    public InterstitialManagerDisplayDelegate getInterstitialDisplayDelegate() {
        return mInterstitialDisplayDelegate;
    }

    public void setAdViewManagerInterstitialDelegate(AdViewManager.AdViewManagerInterstitialDelegate adViewManagerInterstitialDelegate) {
        mAdViewManagerInterstitialDelegate = adViewManagerInterstitialDelegate;
    }

    public void displayPrebidWebViewForMraid(final WebViewBase adBaseView,
                                             final boolean isNewlyLoaded) {

        //if it has come from htmlcreative, then nothing in stack. send closed() callback to pubs
        if (mMraidDelegate != null) {
            mMraidDelegate.displayPrebidWebViewForMraid(adBaseView, isNewlyLoaded, ((WebViewBanner) adBaseView).getMraidEvent());
        }
    }

    // Note: The context should be the Activity this view will display on top of
    public void displayAdViewInInterstitial(Context context, View view) {
        if (!(context instanceof Activity)) {
            OXLog.error(TAG, "displayAdViewInInterstitial(): Can not display interstitial without activity context");
            return;
        }

        if (view instanceof InterstitialView) {
            // TODO: 13.08.2020 Remove casts to specific view
            InterstitialView interstitialView = ((InterstitialView) view);
            show();
            showInterstitialDialog(context, interstitialView);
        }
    }

    public void displayVideoAdViewInInterstitial(Context context, View adView) {
        if (!(context instanceof Activity && adView instanceof VideoView)) {
            OXLog.error(TAG, "displayAdViewInInterstitial(): Can not display interstitial. "
                             + "Context is not activity or adView is not an instance of VideoAdView");
            return;
        }
        show();
    }

    public void destroy() {
        if (mInterstitialDisplayProperties != null) {
            //reset all these for new ads to honour their own creative details
            mInterstitialDisplayProperties.resetExpandValues();
        }

        if (mMraidDelegate != null) {
            mMraidDelegate.destroyMraidExpand();
            mMraidDelegate = null;
        }

        mViewStack.clear();

        mInterstitialDisplayDelegate = null;
    }

    @Override
    public void interstitialAdClosed() {
        if (mInterstitialDisplayDelegate != null) {
            mInterstitialDisplayDelegate.interstitialAdClosed();
        }
        if (mInterstitialVideoDelegate != null) {
            mInterstitialVideoDelegate.onVideoInterstitialClosed();
        }
    }

    @Override
    public void interstitialClosed(View viewToClose) {
        OXLog.debug(TAG, "interstitialClosed");

        try {
            if (!mViewStack.isEmpty() && mMraidDelegate != null) {
                View poppedViewState = mViewStack.pop();
                //take the old one  & display(ex: close of a video inside of an expanded ad
                //should take it back to the defaultview of the ad.
                mMraidDelegate.displayViewInInterstitial(poppedViewState, false, null, null);
                return;
            }

            //check to see if there's something on the backstack we should return to
            //if so, go back or else call interstitialAdClosed
            //IMPORTANT: HAS to be there inspite of calling close above.
            if ((mMraidDelegate == null || !mMraidDelegate.collapseMraid())
                && mInterstitialDialog != null) {
                mInterstitialDialog.nullifyDialog();
                mInterstitialDialog = null;
            }

            if (mMraidDelegate != null) {
                mMraidDelegate.closeThroughJs((WebViewBase) viewToClose);
            }
            //let adview know when an interstitial ad was closed(1part expand or 2part expand are not interstitials)
            //resize->click->expandedview->click on playvideo->pressback->goes back to the expanded state. We should not send clickthrough event to pub, for this case.
            if (mInterstitialDisplayDelegate != null && !(viewToClose instanceof WebViewBanner)) {
                mInterstitialDisplayDelegate.interstitialAdClosed();
            }
        }
        catch (Exception e) {
            OXLog.error(TAG, "InterstitialClosed failed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void interstitialDialogShown(ViewGroup rootViewGroup) {
        if (mInterstitialDisplayDelegate == null) {
            OXLog.debug(TAG, "interstitialDialogShown(): Failed. mInterstitialDelegate == null");
            return;
        }
        mInterstitialDisplayDelegate.interstitialDialogShown(rootViewGroup);
    }

    private void showInterstitialDialog(Context context, InterstitialView interstitialView) {
        WebViewBase webViewBase = ((PrebidWebViewInterstitial) interstitialView.getCreativeView()).getWebView();
        webViewBase.setId(INTERSTITIAL_WEBVIEW_ID);
        mInterstitialDialog = new AdInterstitialDialog(context, webViewBase, interstitialView, this);
        mInterstitialDialog.show();
    }

    public void addOldViewToBackStack(WebViewBase adBaseView, String expandUrl, AdBaseDialog interstitialViewController) {
        View oldWebView = null;

        if (interstitialViewController != null) {
            //at the moment the only case we reach this point is Video opened from expanded MRAID
            adBaseView.sendClickCallBack(expandUrl);
            oldWebView = interstitialViewController.getDisplayView();
        }

        if (oldWebView != null) {
            mViewStack.push(oldWebView);
        }
    }

    public void show() {
        if (mAdViewManagerInterstitialDelegate != null) {
            mAdViewManagerInterstitialDelegate.showInterstitial();
        }
    }

    public HTMLCreative getHtmlCreative() {
        return (HTMLCreative) mInterstitialDisplayDelegate;
    }
}