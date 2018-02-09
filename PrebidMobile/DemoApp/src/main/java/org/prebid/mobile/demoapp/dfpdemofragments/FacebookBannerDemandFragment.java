package org.prebid.mobile.demoapp.dfpdemofragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.prebid.demandsdkadapters.dfp.PrebidCustomEventBanner;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class FacebookBannerDemandFragment extends Fragment {
    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_loadad, null);
        Button btnLoad = (Button) root.findViewById(R.id.load);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshBannerBidUntilReady();
            }
        });
        return root;
    }

    private void refreshBannerBidUntilReady() {
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        //region enable custom event
        builder.addCustomEventExtrasBundle(PrebidCustomEventBanner.class, new Bundle());
        final PublisherAdRequest request = builder.build();
        final Handler handler = new Handler();

        final Runnable attachBids = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                Prebid.attachBids(request, Constants.FACEBOOK_300x250, FacebookBannerDemandFragment.this.getActivity());
                if (request.getCustomTargeting().get("hb_pb") == null && count < 10) {
                    count++;
                    handler.postDelayed(this, 1000);
                } else {
                    FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
                    adFrame.removeAllViews();
                    PublisherAdView adView = new PublisherAdView(getActivity());
                    adView.setAdUnitId("/19968336/Prebid_300x250");
                    adView.setAdSizes(new AdSize(300, 250));
                    adFrame.addView(adView);
                    adView.loadAd(request);
                }
            }
        };
        handler.post(attachBids);
    }
}
