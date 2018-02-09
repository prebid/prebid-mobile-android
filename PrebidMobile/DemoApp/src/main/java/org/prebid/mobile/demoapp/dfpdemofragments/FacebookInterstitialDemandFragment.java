package org.prebid.mobile.demoapp.dfpdemofragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.prebid.demandsdkadapters.dfp.PrebidCustomEventInterstitial;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class FacebookInterstitialDemandFragment extends Fragment {
    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        root = inflater.inflate(R.layout.fragment_loadad, null);

        Button buttonLoadInterstitial = (Button) root.findViewById(R.id.load);
        buttonLoadInterstitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshInterstitialBidUntilReady();
            }
        });
        return root;
    }

    private void refreshInterstitialBidUntilReady() {
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        //region enable custom event
        builder.addCustomEventExtrasBundle(PrebidCustomEventInterstitial.class, new Bundle());
        final PublisherAdRequest request = builder.build();
        final Handler handler = new Handler();

        final Runnable attachBids = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                Prebid.attachBids(request, Constants.FACEBOOK_INTERSTITIAL, FacebookInterstitialDemandFragment.this.getActivity());
                if (request.getCustomTargeting().get("hb_pb") == null && count < 10) {
                    count++;
                    handler.postDelayed(this, 1000);
                } else {
                    final PublisherInterstitialAd interstitialAd = new PublisherInterstitialAd(getContext());
                    interstitialAd.setAdUnitId("/19968336/Prebid_Interstitial");
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            interstitialAd.show();
                        }

                        @Override
                        public void onAdFailedToLoad(int i) {
                            super.onAdFailedToLoad(i);
                        }
                    });
                    interstitialAd.loadAd(request);
                }
            }
        };
        handler.post(attachBids);
    }
}
