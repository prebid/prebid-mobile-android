package org.prebid.mobile.demoapp.mopubdemofragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubErrorCode;

import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

/**
 * Created by nhedley on 7/19/17.
 */

public class MoPubBannerFragment extends Fragment implements Prebid.OnAttachCompleteListener, MoPubView.BannerAdListener {

    private MoPubView adView;
    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_banner, null);

        Button btnLoad = (Button) root.findViewById(R.id.loadBanner);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBanner(v);
            }
        });
        setupBannerWithoutWait();

        return root;
    }

    private void setupBannerWithoutWait() {
        adView = new MoPubView(this.getActivity());
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView.setAdUnitId("a9cb8ff85fef4b50b457e3b11119aabf");
        adView.setBannerAdListener(this);
        adView.setAutorefreshEnabled(true);
        adView.setMinimumWidth(300);
        adView.setMinimumHeight(250);
        adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL));

        adFrame.addView(adView);

        Prebid.attachBids(adView, Constants.MOPUB_BANNER_ADUNIT_ID, this.getActivity());
        adView.loadAd();
    }


    public void loadBanner(View view) {
        if (adView != null) {
            adView.destroy();
            setupBannerWithoutWait();
        }
    }

    @Override
    public void onAttachComplete(Object adObj) {
        if (adView != null) {
            adView.loadAd();
            Prebid.detachUsedBid(adObj);
        }
    }

    // MoPub Banner Listeners
    @Override
    public void onBannerLoaded(MoPubView banner) {
        Log.d("SampleActivity", "onBannerLoaded");
        if (banner.getAdUnitId() == Constants.MOPUB_BANNER_ADUNIT_ID) {
            //To handle auto-refresh, enable prebid again for the current banner adView to load next ad.
            Prebid.attachBids(banner, Constants.MOPUB_BANNER_ADUNIT_ID, this.getActivity());
        }
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.d("SampleActivity", "onBannerFailed");
        if (banner.getAdUnitId() == Constants.MOPUB_BANNER_ADUNIT_ID) {
            //To handle auto-refresh, enable prebid again for the current banner adView to load next ad.
            Prebid.attachBids(banner, Constants.MOPUB_BANNER_ADUNIT_ID, this.getActivity());
        }
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adView != null) {
            adView.destroy();
        }
    }
}
