package org.prebid.mobile.demoapp.dfpdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;


public class DFPBannerFragment extends Fragment implements Prebid.OnAttachCompleteListener {
    PublisherAdView adView1;
    PublisherAdView adView2;
    PublisherAdRequest request1;
    PublisherAdRequest request2;
    private View root;
    private AdListener adListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_banner, null);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        request1 = builder.build();
        setupBannerWithoutWait(request1);

        PublisherAdRequest.Builder builder2 = new PublisherAdRequest.Builder();
        request2 = builder2.build();
        setupBannerWithWait(request2, 500);

        Button btnLoad = (Button) root.findViewById(R.id.loadBanner);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBanner(v);
            }
        });
        adListener = new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                LogUtil.d("DPF-Banner", "OnAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                LogUtil.d("DPF-Banner", "OnAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                LogUtil.d("DPF-Banner", "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                LogUtil.d("DPF-Banner", "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                LogUtil.d("DPF-Banner", "onAdLoaded");
            }
        };
        return root;
    }

    private void setupBannerWithoutWait(PublisherAdRequest request) {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView1 = new PublisherAdView(getActivity());
        adView1.setAdUnitId(Constants.DFP_BANNER_ADUNIT_320x50);
        adView1.setAdSizes(new AdSize(320, 50));
        adView1.setAdListener(adListener);
        adFrame.addView(adView1);
        //region PriceCheckForDFP API usage
        Prebid.attachBids(request, Constants.DFP_BANNER_ADUNIT_320x50, this.getActivity());
        //endregion
        adView1.loadAd(request);
    }

    private void setupBannerWithWait(final PublisherAdRequest request, final int waitTime) {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame2);
        adFrame.removeAllViews();
        adView2 = new PublisherAdView(getActivity());
        adView2.setAdUnitId(Constants.DFP_BANNER_ADUNIT_300x250);
        adView2.setAdSizes(new AdSize(300, 250));
        adView2.setAdListener(adListener);
        adFrame.addView(adView2);
        //region PriceCheckForDFP API usage
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        request1 = builder.build();
        Prebid.attachBids(request1, Constants.DFP_BANNER_ADUNIT_300x250, this.getActivity());
        adView2.loadAd(request1);
        //PrebidMobileForDFP.attachTopBidWhenReady(request, Constants.DFP_BANNER_ADUNIT_300x250, getContext(), waitTime, this);
        //endregion

    }

    public void loadBanner(View view) {
        if (adView1 != null && request1 != null) {
//            PrebidMobileForDFP.attachTopBid(request1, Constants.DFP_BANNER_ADUNIT_320x50, getContext());
            adView1.destroy();
            setupBannerWithoutWait(request1);
        }
        if (adView2 != null && request2 != null) {
//            PrebidMobileForDFP.attachTopBid(request2, Constants.DFP_BANNER_ADUNIT_300x250, getContext());
//            adView2.loadAd(request2);
            adView2.destroy();
            setupBannerWithWait(request2, 500);
        }
    }

    @Override
    public void onAttachComplete(Object adObj) {
        if (adView2 != null && adObj != null && adObj instanceof PublisherAdRequest) {
            adView2.loadAd((PublisherAdRequest) adObj);
            Prebid.detachUsedBid(adObj);
        }
    }
}
