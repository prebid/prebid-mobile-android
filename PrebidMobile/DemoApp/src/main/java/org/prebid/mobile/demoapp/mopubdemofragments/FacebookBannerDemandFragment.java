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

import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class FacebookBannerDemandFragment extends Fragment {
    private View root;
    private MoPubView adView;


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
                Prebid.attachBids(adView, Constants.FACEBOOK_300x250, FacebookBannerDemandFragment.this.getActivity());
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

}
