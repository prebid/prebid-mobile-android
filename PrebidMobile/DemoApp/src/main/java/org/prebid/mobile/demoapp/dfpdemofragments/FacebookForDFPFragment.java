package org.prebid.mobile.demoapp.dfpdemofragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.prebid.demandsdkadapters.dfp.PrebidCustomEventBanner;
import org.prebid.demandsdkadapters.dfp.PrebidCustomEventInterstitial;
import org.prebid.demandsdkadapters.dfp.PrebidCustomEventSettings;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class FacebookForDFPFragment extends Fragment {
    private View root;

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
                loadDFPBanner();
            }
        });
        TextView buttonLoadInterstitial = (Button) root.findViewById(R.id.loadInterstitial);
        buttonLoadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDFPInterstitial();
            }
        });
        return root;
    }

    private void loadDFPBanner() {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        PublisherAdView adView = new PublisherAdView(getActivity());
        adView.setAdUnitId("/19968336/Prebid_300x250");
        adView.setAdSizes(new AdSize(300, 250));
        adFrame.addView(adView);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        //region enable custom event
        builder.addCustomEventExtrasBundle(PrebidCustomEventBanner.class, new Bundle());
        builder.addKeyword("prebid_banner");
        PublisherAdRequest request = builder.build();
//        Prebid.attachBids(request, Constants.FACEBOOK_300x250, this.getActivity());
        Prebid.attachBids(request, Constants.BANNER_300x250, this.getActivity()); // todo change this back to test facebook adapter
        //endregion
        adView.loadAd(request);
    }

    private void loadDFPInterstitial() {
        final PublisherInterstitialAd interstitialAd = new PublisherInterstitialAd(getContext());
        interstitialAd.setAdUnitId("/19968336/PriceCheck_Interstitial");
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitialAd.show();
            }
        });
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        //region enable custom event
        builder.addCustomEventExtrasBundle(PrebidCustomEventInterstitial.class, new Bundle());
        builder.addKeyword("prebid_interstitial");
        PublisherAdRequest request = builder.build();
        Prebid.attachBids(request, Constants.BANNER_300x250, this.getActivity());
        //endregion
        interstitialAd.loadAd(request);
    }
}
