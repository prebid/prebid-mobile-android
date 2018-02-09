package org.prebid.mobile.demoapp.mopubdemofragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;


public class FacebookInterstitialDemandFragment extends Fragment {
    private View root;
    private MoPubInterstitial interstitialAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_loadad, null);
        Button loadInterstitial = (Button) root.findViewById(R.id.load);
        loadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshInterstitialBidUntilReady();
            }
        });
        return root;
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

}
