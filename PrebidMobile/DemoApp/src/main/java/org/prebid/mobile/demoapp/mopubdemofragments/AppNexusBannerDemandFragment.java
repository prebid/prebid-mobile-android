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

public class AppNexusBannerDemandFragment extends Fragment implements Prebid.OnAttachCompleteListener, MoPubView.BannerAdListener {

    private MoPubView adView;
    private View root;
    int w;
    int h;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_loadad, null);
        w = getArguments().getInt(Constants.WIDTH);
        h = getArguments().getInt(Constants.HEIGHT);
        Button btnLoad = (Button) root.findViewById(R.id.load);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBanner();
            }
        });
        setupBannerWithWait(500);
        return root;
    }


    private void setupBannerWithWait(final int waitTime) {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView = new MoPubView(this.getActivity());
        adView.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
        adView.setBannerAdListener(this);
        adView.setAutorefreshEnabled(true);
        adView.setMinimumWidth(w);
        adView.setMinimumHeight(h);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        adView.setLayoutParams(lp);
        adFrame.addView(adView);
        //region Prebid API usage
        Prebid.attachBidsWhenReady(adView, Constants.BANNER_300x250, this, waitTime, this.getActivity());
        //endregion
    }

    public void loadBanner() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        setupBannerWithWait(500);
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
    }
}
