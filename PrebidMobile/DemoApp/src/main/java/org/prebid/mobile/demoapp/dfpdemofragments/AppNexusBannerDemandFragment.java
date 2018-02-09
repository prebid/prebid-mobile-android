package org.prebid.mobile.demoapp.dfpdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;


public class AppNexusBannerDemandFragment extends Fragment implements Prebid.OnAttachCompleteListener {
    PublisherAdView adView1;
    PublisherAdView adView;
    private View root;
    private AdListener adListener;
    private int w;
    private int h;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        w = getArguments().getInt(Constants.WIDTH);
        h = getArguments().getInt(Constants.HEIGHT);
        root = inflater.inflate(R.layout.fragment_loadad, null);
        setupBannerWithWait(500);
        Button btnLoad = (Button) root.findViewById(R.id.load);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAd();
            }
        });
        adListener = new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                LogUtil.d("DFP-Banner", "OnAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                LogUtil.d("DFP-Banner", "OnAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                LogUtil.d("DFP-Banner", "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                LogUtil.d("DFP-Banner", "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                LogUtil.d("DFP-Banner", "onAdLoaded");
            }
        };

        return root;
    }


    public String getDFPWebViewName() {
        int count = adView.getChildCount();
        for (int i = 0; i < count; i++) {
            ViewGroup nextChild = (ViewGroup) adView.getChildAt(i);
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
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView = new PublisherAdView(getActivity());
        adView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_300x250);
        adView.setAdSizes(new AdSize(w, h));
        adView.setAdListener(adListener);
        adFrame.addView(adView);
        //region PriceCheckForDFP API usage
        String code = "";
        if (w == 300 && h == 250) {
            code = Constants.BANNER_300x250;
        } else if (w == 320 && h == 250) {
            code = Constants.BANNER_320x50;
        }
        Prebid.attachBidsWhenReady(new PublisherAdRequest.Builder().build(), code, this, waitTime, this.getActivity());
        //endregion

    }

    public void loadAd() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        setupBannerWithWait(500);
    }

    @Override
    public void onAttachComplete(Object adObj) {
        if (adView != null && adObj != null && adObj instanceof PublisherAdRequest) {
            adView.loadAd((PublisherAdRequest) adObj);
            Prebid.detachUsedBid(adObj);
        }
    }
}
