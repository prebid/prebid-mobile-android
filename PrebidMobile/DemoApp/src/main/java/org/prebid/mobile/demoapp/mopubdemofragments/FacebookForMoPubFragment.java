package org.prebid.mobile.demoapp.mopubdemofragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class FacebookForMoPubFragment extends Fragment {
    private View root;
    private MoPubView adView;
    private MoPubInterstitial interstitialAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_facebook, null);
        Button btnLoad = (Button) root.findViewById(R.id.loadBanner);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshBannerBidUntilReady();
            }
        });
        Button loadInterstitial = (Button) root.findViewById(R.id.loadInterstitial);
        loadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshInterstitialBidUntilReady();
            }
        });
        return root;
    }

    private void refreshBannerBidUntilReady() {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        if (adView != null) {
            adView.destroy();
        }
        adView = new MoPubView(this.getActivity());
        adView.setAdUnitId("bcef78630d754295a1a7757b434941d1");
        adView.setAutorefreshEnabled(true);
        adView.setMinimumWidth(300);
        adView.setMinimumHeight(250);
        adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        adFrame.addView(adView);
        final Handler handler = new Handler();
        final Runnable attachBids = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                Prebid.attachBids(adView, Constants.FACEBOOK_300x250, FacebookForMoPubFragment.this.getActivity());
                if ((adView.getKeywords() == null || !adView.getKeywords().contains("hb_pb")) && count < 10) {
                    count++;
                    handler.postDelayed(this, 1000);
                } else {
                    adView.loadAd();
                }
            }
        };
        handler.post(attachBids);

    }

    private void refreshInterstitialBidUntilReady() {
        if (interstitialAdView != null) {
            interstitialAdView.destroy();
        }
        interstitialAdView = new MoPubInterstitial(this.getActivity(), "1bfc4a07a3054cac9d349a072a171173");
        interstitialAdView.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                if (interstitialAdView.isReady()) {
                    interstitialAdView.show();
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {

            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {

            }
        });
        final Handler handler = new Handler();
        final Runnable attachBids = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                Prebid.attachBids(interstitialAdView, Constants.FACEBOOK_INTERSTITIAL, getContext());
                if ((interstitialAdView.getKeywords() == null || !interstitialAdView.getKeywords().contains("hb_pb")) && count < 10) {
                    count++;
                    handler.postDelayed(this, 1000);
                } else {
                    interstitialAdView.load();
                }
            }
        };
        handler.post(attachBids);

    }

    private void loadMoPubBanner() {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        if (adView != null) {
            adView.destroy();
        }
        adView = new MoPubView(this.getActivity());
        adView.setAdUnitId("bcef78630d754295a1a7757b434941d1");
        adView.setAutorefreshEnabled(true);
        adView.setMinimumWidth(300);
        adView.setMinimumHeight(250);
        adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        adFrame.addView(adView);
        Prebid.attachBids(adView, Constants.FACEBOOK_300x250, this.getActivity());
        adView.loadAd();
    }

    private void loadMoPubInterstitial() {
        if (interstitialAdView != null) {
            interstitialAdView.destroy();
        }
        interstitialAdView = new MoPubInterstitial(this.getActivity(), "1bfc4a07a3054cac9d349a072a171173");
        Prebid.attachBids(interstitialAdView, Constants.FACEBOOK_INTERSTITIAL, getContext());
        interstitialAdView.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                if (interstitialAdView.isReady()) {
                    interstitialAdView.show();
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {

            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {

            }
        });
        interstitialAdView.load();
        Prebid.detachUsedBid(interstitialAdView);
    }
}
