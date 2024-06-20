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
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.api.rendering.VideoView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.interstitial.*;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.mraid.methods.InterstitialManagerMraidDelegate;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.rendering.views.webview.WebViewBanner;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

import java.util.Stack;

public class InterstitialManager implements InterstitialManagerInterface {

    private static String TAG = InterstitialManager.class.getSimpleName();
    private static final int INTERSTITIAL_WEBVIEW_ID = 123456789;

    private InterstitialDisplayPropertiesInternal interstitialDisplayProperties = new InterstitialDisplayPropertiesInternal();
    private AdInterstitialDialog interstitialDialog;

    private InterstitialManagerDisplayDelegate interstitialDisplayDelegate;
    private InterstitialManagerVideoDelegate interstitialVideoDelegate;

    private InterstitialManagerMraidDelegate mraidDelegate;
    private AdViewManager.AdViewManagerInterstitialDelegate adViewManagerInterstitialDelegate;

    private Stack<View> viewStack = new Stack<>();

    public void setMraidDelegate(InterstitialManagerMraidDelegate mraidDelegate) {
        this.mraidDelegate = mraidDelegate;
    }

    public void configureInterstitialProperties(AdUnitConfiguration adConfiguration) {
        InterstitialLayoutConfigurator.configureDisplayProperties(adConfiguration, interstitialDisplayProperties);
    }

    public InterstitialDisplayPropertiesInternal getInterstitialDisplayProperties() {
        return interstitialDisplayProperties;
    }

    public void setInterstitialDisplayDelegate(InterstitialManagerDisplayDelegate interstitialDisplayDelegate) {
        this.interstitialDisplayDelegate = interstitialDisplayDelegate;
    }

    public void setInterstitialVideoDelegate(InterstitialManagerVideoDelegate interstitialVideoDelegate) {
        this.interstitialVideoDelegate = interstitialVideoDelegate;
    }

    @VisibleForTesting
    public InterstitialManagerDisplayDelegate getInterstitialDisplayDelegate() {
        return interstitialDisplayDelegate;
    }

    public void setAdViewManagerInterstitialDelegate(AdViewManager.AdViewManagerInterstitialDelegate adViewManagerInterstitialDelegate) {
        this.adViewManagerInterstitialDelegate = adViewManagerInterstitialDelegate;
    }

    public void displayPrebidWebViewForMraid(final WebViewBase adBaseView,
                                             final boolean isNewlyLoaded) {

        //if it has come from htmlcreative, then nothing in stack. send closed() callback to pubs
        if (mraidDelegate != null) {
            mraidDelegate.displayPrebidWebViewForMraid(
                    adBaseView,
                    isNewlyLoaded,
                    ((WebViewBanner) adBaseView).getMraidEvent()
            );
        }
    }

    // Note: The context should be the Activity this view will display on top of
    public void displayAdViewInInterstitial(Context context, View view) {
        if (!(context instanceof Activity)) {
            LogUtil.error(TAG, "displayAdViewInInterstitial(): Can not display interstitial without activity context");
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
            LogUtil.error(TAG, "displayAdViewInInterstitial(): Can not display interstitial. "
                    + "Context is not activity or adView is not an instance of VideoAdView");
            return;
        }
        show();
    }

    public void destroy() {
        if (interstitialDisplayProperties != null) {
            //reset all these for new ads to honour their own creative details
            interstitialDisplayProperties.resetExpandValues();
        }

        if (mraidDelegate != null) {
            mraidDelegate.destroyMraidExpand();
            mraidDelegate = null;
        }

        viewStack.clear();

        interstitialDisplayDelegate = null;
    }

    @Override
    public void interstitialAdClosed() {
        if (interstitialDisplayDelegate != null) {
            interstitialDisplayDelegate.interstitialAdClosed();
        }
        if (interstitialVideoDelegate != null) {
            interstitialVideoDelegate.onVideoInterstitialClosed();
        }
    }

    @Override
    public void interstitialClosed(View viewToClose) {
        LogUtil.debug(TAG, "interstitialClosed");

        try {
            if (!viewStack.isEmpty() && mraidDelegate != null) {
                View poppedViewState = viewStack.pop();
                //take the old one  & display(ex: close of a video inside of an expanded ad
                //should take it back to the defaultview of the ad.
                mraidDelegate.displayViewInInterstitial(poppedViewState, false, null, null);
                return;
            }

            //check to see if there's something on the backstack we should return to
            //if so, go back or else call interstitialAdClosed
            //IMPORTANT: HAS to be there inspite of calling close above.
            if ((mraidDelegate == null || !mraidDelegate.collapseMraid()) && interstitialDialog != null) {
                interstitialDialog.nullifyDialog();
                interstitialDialog = null;
            }

            if (mraidDelegate != null) {
                mraidDelegate.closeThroughJs((WebViewBase) viewToClose);
            }
            //let adview know when an interstitial ad was closed(1part expand or 2part expand are not interstitials)
            //resize->click->expandedview->click on playvideo->pressback->goes back to the expanded state. We should not send clickthrough event to pub, for this case.
            if (interstitialDisplayDelegate != null && !(viewToClose instanceof WebViewBanner)) {
                interstitialDisplayDelegate.interstitialAdClosed();
            }
        }
        catch (Exception e) {
            LogUtil.error(TAG, "InterstitialClosed failed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void interstitialDialogShown(ViewGroup rootViewGroup) {
        if (interstitialDisplayDelegate == null) {
            LogUtil.debug(TAG, "interstitialDialogShown(): Failed. interstitialDelegate == null");
            return;
        }
        interstitialDisplayDelegate.interstitialDialogShown(rootViewGroup);
    }

    private void showInterstitialDialog(Context context, InterstitialView interstitialView) {
        WebViewBase webViewBase = ((PrebidWebViewInterstitial) interstitialView.getCreativeView()).getWebView();
        webViewBase.setId(INTERSTITIAL_WEBVIEW_ID);
        interstitialDialog = new AdInterstitialDialog(context, webViewBase, interstitialView, this);
        interstitialDialog.show();
    }

    public void addOldViewToBackStack(WebViewBase adBaseView, String expandUrl, AdBaseDialog interstitialViewController) {
        View oldWebView = null;

        if (interstitialViewController != null) {
            //at the moment the only case we reach this point is Video opened from expanded MRAID
            adBaseView.sendClickCallBack(expandUrl);
            oldWebView = interstitialViewController.getDisplayView();
        }

        if (oldWebView != null) {
            viewStack.push(oldWebView);
        }
    }

    public void show() {
        if (adViewManagerInterstitialDelegate != null) {
            adViewManagerInterstitialDelegate.showInterstitial();
        }
    }

    public HTMLCreative getHtmlCreative() {
        return (HTMLCreative) interstitialDisplayDelegate;
    }
}