package org.prebid.mobile.demoapp.dfpdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.prebid.mobile.NewBannerAdUnit;
import org.prebid.mobile.NewOnCompleteListener;
import org.prebid.mobile.NewPrebid;
import org.prebid.mobile.NewResultCode;
import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;


public class DFPBannerFragment extends Fragment implements Prebid.OnAttachCompleteListener {
    PublisherAdView adView1;
    PublisherAdView dfpAdView;
    private View root;
    private AdListener adListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_banner, null);

        setupBannerWithoutWait();

        setupBannerWithWait(500);

        Button btnLoad = (Button) root.findViewById(R.id.loadBanner);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBanner();
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

    private void setupBannerWithoutWait() {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView1 = new PublisherAdView(getActivity());
        adView1.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_320x50);
        adView1.setAdSizes(new AdSize(320, 50));
        adView1.setAdListener(adListener);
        adFrame.addView(adView1);
        //region PriceCheckForDFP API usage
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        PublisherAdRequest request = builder.build();
        Prebid.attachBids(request, Constants.BANNER_320x50, this.getActivity());
        //endregion
        adView1.loadAd(request);
    }


    public String getDFPWebViewName() {
        int count = dfpAdView.getChildCount();
        for (int i = 0; i < count; i++) {
            ViewGroup nextChild = (ViewGroup) dfpAdView.getChildAt(i);
            int secondCount = nextChild.getChildCount();
            for (int j = 0; j < secondCount; j++) {
                ViewGroup thirdChild = (ViewGroup) nextChild.getChildAt(j);
                int thirdCount = thirdChild.getChildCount();
                for (int k = 0; k < thirdCount; k++) {
                    System.out.println(thirdChild.getChildAt(k));
                    if (thirdChild.getChildAt(k) instanceof WebView) {
                        return thirdChild.getChildAt(k).getClass().getName();
                    }
                }
            }
        }
        return "undefined";
    }

    private void setupBannerWithWait(final int waitTime) {

        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame2);
        adFrame.removeAllViews();
        dfpAdView = new PublisherAdView(getActivity());
        dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_300x250);
        dfpAdView.setAdSizes(new AdSize(300, 250));
        dfpAdView.setAdListener(adListener);
        adFrame.addView(dfpAdView);
        //region Prebid Mobile API usage
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        final PublisherAdRequest request = builder.build();
        Prebid.attachBidsWhenReady(request, Constants.BANNER_300x250, this, waitTime, this.getActivity());
        //endregion

    }

    public void loadBanner() {
        if (adView1 != null) {
            adView1.destroy();
            setupBannerWithoutWait();
        }
        if (dfpAdView != null) {
            dfpAdView.destroy();
            setupBannerWithWait(500);
        }
    }

    @Override
    public void onAttachComplete(Object adObj) {
        if (dfpAdView != null && adObj != null && adObj instanceof PublisherAdRequest) {
            dfpAdView.loadAd((PublisherAdRequest) adObj);
            Prebid.detachUsedBid(adObj);
        }
    }
}
