package org.prebid.mobile.demoapp.mopubdemofragments;

import android.os.Bundle;
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
        try {
            PrebidCustomEventSettings.enableDemand(PrebidCustomEventSettings.Demand.FACEBOOK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button btnLoad = (Button) root.findViewById(R.id.loadBanner);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoPubBanner();
            }
        });
        Button loadInterstitial = (Button) root.findViewById(R.id.loadInterstitial);
        loadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoPubInterstitial();
            }
        });
        return root;
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
        Prebid.attachBids(interstitialAdView, Constants.INTERSTITIAL_ADUNIT_ID, getContext()); // todo update this
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
