package org.prebid.mobile.demoapp.dfpdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class DFPInterstitialFragment extends Fragment {
    View root;
    PublisherAdRequest request;
    PublisherInterstitialAd mPublisherInterstitialAd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_interstitial, null);
        // interstitial set up
        mPublisherInterstitialAd = new PublisherInterstitialAd(getContext());
        mPublisherInterstitialAd.setAdUnitId("/19968336/PriceCheck_Interstitial");
        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (i == AdRequest.ERROR_CODE_INTERNAL_ERROR) {
                    LogUtil.e("DFP", "Interstitial Ad failed to load internal error");
                } else if (i == AdRequest.ERROR_CODE_INVALID_REQUEST) {
                    LogUtil.e("DFP", "Interstitial Ad failed to load internal error");
                } else if (i == AdRequest.ERROR_CODE_NETWORK_ERROR) {
                    LogUtil.e("DFP", "Interstitial Ad failed to load network error");
                } else if (i == AdRequest.ERROR_CODE_NO_FILL) {
                    LogUtil.e("DFP", "Interstitial Ad failed to load no fill");
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mPublisherInterstitialAd.show();
            }
        });
        request = new PublisherAdRequest.Builder().build();
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
        Prebid.attachBids(request, Constants.INTERSTITIAL_ADUNIT_ID, getContext());
        mPublisherInterstitialAd.loadAd(request);
        Prebid.detachUsedBid(request);
    }
}
