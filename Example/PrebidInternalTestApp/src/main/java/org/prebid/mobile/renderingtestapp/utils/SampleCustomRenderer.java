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

package org.prebid.mobile.renderingtestapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobilePluginCustomRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

public class SampleCustomRenderer implements PrebidMobilePluginCustomRenderer {

    @Override
    public String getName() {
        return "SampleCustomRenderer";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public View createBannerAdView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        FrameLayout frameLayout = new FrameLayout(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        frameLayout.setLayoutParams(layoutParams);
        frameLayout.setBackgroundColor(Color.parseColor("#D3D3D3"));
        frameLayout.setOnClickListener(view -> { displayViewListener.onAdClicked(); });
        TextView textView = new TextView(context);
        textView.setText(bidResponse.getWinningBidJson());
        textView.setGravity(Gravity.CENTER);

        ImageButton closeButton = new ImageButton(context);
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setBackgroundColor(Color.parseColor("#000000"));
        FrameLayout.LayoutParams closeButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END
        );
        closeButtonParams.setMargins(0, 20, 20, 0);
        closeButton.setLayoutParams(closeButtonParams);
        closeButton.setOnClickListener(view -> {
          displayViewListener.onAdClosed();
          frameLayout.setVisibility(View.GONE);
        });

        frameLayout.addView(textView);
        frameLayout.addView(closeButton);

        displayViewListener.onAdDisplayed();
        return frameLayout;
    }

    @Override
    public PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener interstitialControllerListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse response
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Interstitial");
        builder.setMessage(response.getWinningBidJson());
        builder.setPositiveButton("Ok", (dialog, which) -> {
            interstitialControllerListener.onInterstitialClicked();
        });
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setOnDismissListener(dialog -> interstitialControllerListener.onInterstitialClosed());
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();

        return new PrebidMobileInterstitialControllerInterface() {
            @Override
            public void loadAd(AdUnitConfiguration adUnitConfiguration, BidResponse bidResponse) {
                Toast.makeText(context, "Load Interstitial Ad", Toast.LENGTH_LONG).show();
                interstitialControllerListener.onInterstitialReadyForDisplay();
            }

            @Override
            public void show() {
                alertDialog.show();
                interstitialControllerListener.onInterstitialDisplayed();
            }

            @Override
            public void destroy() {

            }
        };
    }

    @Override
    public boolean isSupportRenderingFor(AdUnitConfiguration adUnitConfiguration) {
        return adUnitConfiguration.isAdType(AdFormat.BANNER)
        || adUnitConfiguration.isAdType(AdFormat.INTERSTITIAL);
    }
}
