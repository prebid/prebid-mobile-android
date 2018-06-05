package org.prebid.mobile.demoapp.adformdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.adform.sdk.pub.AdOverlay;

import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;

public class AdformInterstitialFragment extends Fragment {
    View root;
    AdOverlay adOverlay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_interstitial, null);

        adOverlay = AdOverlay.createInstance(getActivity());
        adOverlay.setMasterTagId(142636);
        adOverlay.setListener(new AdOverlay.OverlayLoaderListener() {
            @Override
            public void onLoadSuccess() {
                adOverlay.showAd();
            }

            @Override
            public void onLoadError(String error) {
                LogUtil.e("Adform", error);
            }

            @Override
            public void onShowError(String error) {
                LogUtil.e("Adform", error);
            }
        });

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
        Prebid.attachBids(adOverlay, Constants.INTERSTITIAL_FULLSCREEN, getContext());
        adOverlay.loadAd();
        Prebid.detachUsedBid(adOverlay);
    }

    @Override
    public void onResume() {
        super.onResume();
        adOverlay.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        adOverlay.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adOverlay.destroy();
    }
}
