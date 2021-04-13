package org.prebid.mobile.rendering.interstitial;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.indicator.AdIndicatorView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

//Used for MRAID ad expansion
public class AdExpandedDialog extends AdBaseDialog {
    private static final String TAG = AdExpandedDialog.class.getSimpleName();

    public AdExpandedDialog(final Context context, final WebViewBase webViewBaseLocal, InterstitialManager interstitialManager) {
        super(context, webViewBaseLocal, interstitialManager);

        //On MRAID expand we should not remove the old adview such that when the user closes the expanded ad
        //they see the old ad.

        preInit();

        if (mWebViewBase != null && mWebViewBase.isMRAID()) {
            mWebViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_EXPANDED);
        }
        setOnCancelListener(dialog -> {
            try {
                if (mWebViewBase != null) {
                    //detach from closecontainer
                    mWebViewBase.detachFromParent();
                    //add it back to OXWebView.
                    PrebidWebViewBase defaultContainer = (PrebidWebViewBase) mWebViewBase.getPreloadedListener();

                    //use getPreloadedListener() to get defaultContainer, as mDefaultContainer is not initiated for non-mraid cases(such as interstitials)
                    defaultContainer.addView(mWebViewBase);
                    ////IMP - get the default state
                    defaultContainer.setVisibility(View.VISIBLE);
                    //do not ever call prebidWebView.visible. It makes the default adview on click of expand to be blank.
                    if (context instanceof Activity) {
                        ((Activity) context).setRequestedOrientation(mInitialOrientation);
                    }
                    else {
                        OXLog.error(TAG, "Context is not Activity, can not set orientation");
                    }

                    HTMLCreative creative = defaultContainer.getCreative();
                    if (creative != null && creative.getAdIndicatorView() != null) {
                        mAdIndicatorView = creative.getAdIndicatorView();
                        ((AdIndicatorView) mAdIndicatorView).setPosition(AdIndicatorView.AdIconPosition.TOP);
                        Views.removeFromParent(mAdIndicatorView);
                        if (!mWebViewBase.getJSName().equals("twopart")) {
                            defaultContainer.addView(mAdIndicatorView);
                        }
                        else {
                            ((PrebidWebViewBase) defaultContainer.getOldWebView().getPreloadedListener()).addView(mAdIndicatorView);
                        }
                    }

                    mWebViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_DEFAULT);
                }
            }
            catch (Exception e) {
                OXLog.error(TAG, "Expanded ad closed but post-close events failed: " + Log.getStackTraceString(e));
            }
        });

        mWebViewBase.setDialog(this);
    }

    @Override
    protected void handleCloseClick() {
        mInterstitialManager.interstitialClosed(mWebViewBase);
    }

    @Override
    protected void handleDialogShow() {
        Views.removeFromParent(mAdViewContainer);
        if (mAdIndicatorView != null) {
            Views.removeFromParent(mAdIndicatorView);
            mAdViewContainer.addView(mAdIndicatorView);
        }
        addContentView(mAdViewContainer,
                                   new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                   RelativeLayout.LayoutParams.MATCH_PARENT)
        );
    }

    public void showAdIndicator() {
        if (mAdIndicatorView == null) {
            if (mWebViewBase != null) {
                mAdIndicatorView = ((PrebidWebViewBase) mWebViewBase.getPreloadedListener()).getCreative().getAdIndicatorView();
                ((AdIndicatorView) mAdIndicatorView).setPosition(AdIndicatorView.AdIconPosition.BOTTOM);
            }
        }
    }
}
