package org.prebid.mobile.demoapp.mopubdemofragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.R;
import org.prebid.mobile.demoapp.Constants;

/**
 * Created by nhedley on 7/19/17.
 */

public class MoPubInterstitialFragment extends Fragment implements Prebid.OnAttachCompleteListener, MoPubInterstitial.InterstitialAdListener {
    MoPubInterstitial interstitialAdView;
    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_interstitial, null);

        interstitialAdView = new MoPubInterstitial(this.getActivity(), "fc82df5c964945c79cceef4c5666e1e2");

        Button btnLoad = (Button) root.findViewById(R.id.loadInterstitial);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInterstitial(v);
            }
        });

        return root;
    }

    public void loadInterstitial(View view) {
        Prebid.attachBids(interstitialAdView, Constants.INTERSTITIAL_ADUNIT_ID, getContext());
        interstitialAdView.setInterstitialAdListener(this);
        interstitialAdView.load();
        Prebid.detachUsedBid(interstitialAdView);
    }

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

    @Override
    public void onAttachComplete(Object adObj) {
        interstitialAdView.load();
        Prebid.detachUsedBid(adObj);
    }
}
