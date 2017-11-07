package org.prebid.mobile.demoapp.mopubdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

import java.util.HashMap;

public class MoPubBannerFragment extends Fragment implements Prebid.OnAttachCompleteListener, MoPubView.BannerAdListener {

    private MoPubView adView;
    private MoPubView adView2;
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
                loadBanner();
            }
        });
        setupBannerWithoutWait();
        setupBannerWithWait(500);
        return root;
    }

    private void setupBannerWithoutWait() {
        adView = new MoPubView(this.getActivity());
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView.setAdUnitId(Constants.MOPUB_AD_UNIT_ID_1);
        adView.setBannerAdListener(this);
        adView.setAutorefreshEnabled(true);
        adView.setMinimumWidth(320);
        adView.setMinimumHeight(50);
        adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        adFrame.addView(adView);
        Prebid.attachBids(adView, Constants.BANNER_320x50, this.getActivity());
        adView.loadAd();
    }


    private void setupBannerWithWait(final int waitTime) {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame2);
        adFrame.removeAllViews();
        adView2 = new MoPubView(this.getActivity());
        adView2.setAdUnitId(Constants.MOPUB_AD_UNIT_ID_2);
        adView2.setBannerAdListener(this);
        adView2.setAutorefreshEnabled(true);
        adView2.setMinimumWidth(300);
        adView2.setMinimumHeight(250);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        adView2.setLayoutParams(lp);
        adFrame.addView(adView2);
        //region Prebid API usage
        Prebid.attachBidsWhenReady(adView2, Constants.BANNER_300x250, this, waitTime, this.getActivity());
        //endregion

    }

    public void loadBanner() {
        if (adView != null) {
            adView.destroy();
            setupBannerWithoutWait();
        }
        if (adView2 != null) {
            adView2.destroy();
            setupBannerWithWait(500);
        }
    }

    @Override
    public void onAttachComplete(Object adObj) {
        if (adObj != null && adObj instanceof MoPubView) {
            ((MoPubView) adObj).loadAd();
        }
    }

    // MoPub Banner Listeners
    @Override
    public void onBannerLoaded(MoPubView banner) {
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
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
        if (adView2 != null) {
            adView2.destroy();
        }
    }
}
