package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.PrebidDestroyable;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.networking.WinNotifier;

/**
 * Mediation banner container that lets adapters render through the preferred plugin renderer.
 */
public class MediationBannerView extends FrameLayout implements PrebidDestroyable {

    private static final String TAG = MediationBannerView.class.getSimpleName();

    @Nullable
    private View adView;
    @Nullable
    private AdUnitConfiguration adUnitConfiguration;
    @Nullable
    private DisplayViewListener displayViewListener;
    @Nullable
    private DisplayVideoListener displayVideoListener;

    public MediationBannerView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        this(context, displayViewListener, null, adUnitConfiguration, bidResponse);
    }

    public MediationBannerView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @Nullable DisplayVideoListener displayVideoListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        super(context);

        this.adUnitConfiguration = adUnitConfiguration;
        this.displayViewListener = displayViewListener;
        this.displayVideoListener = displayVideoListener;

        createBannerAdView(context, bidResponse);
    }

    private void createBannerAdView(
            @NonNull Context context,
            @NonNull BidResponse bidResponse
    ) {
        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(bidResponse, () -> {
            if (adUnitConfiguration == null || displayViewListener == null) {
                return;
            }

            adView = PluginRendererFactory.createBannerAdView(
                    context,
                    displayViewListener,
                    displayVideoListener,
                    adUnitConfiguration,
                    bidResponse
            );

            if (adView == null) {
                displayViewListener.onAdFailed(new AdException(
                        AdException.INTERNAL_ERROR,
                        "Renderer returned null banner view"
                ));
                return;
            }

            addView(adView);
        });
    }

    @Override
    public void destroy() {
        LogUtil.debug(TAG, "Destroying mediation banner view");
        if (adView instanceof PrebidDestroyable) {
            ((PrebidDestroyable) adView).destroy();
        }
        removeAllViews();
        adView = null;
        adUnitConfiguration = null;
        displayViewListener = null;
        displayVideoListener = null;
    }
}
