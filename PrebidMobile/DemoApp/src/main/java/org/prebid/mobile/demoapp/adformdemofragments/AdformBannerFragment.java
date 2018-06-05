package org.prebid.mobile.demoapp.adformdemofragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.adform.sdk.interfaces.AdListener;
import com.adform.sdk.pub.views.AdInline;
import com.adform.sdk.utils.AdSize;

import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.demoapp.Constants;
import org.prebid.mobile.demoapp.R;


public class AdformBannerFragment extends Fragment implements Prebid.OnAttachCompleteListener {

    AdInline adInline;
    AdInline adInline2;
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
            public void onAdLoadSuccess(AdInline adInline) {
                Prebid.detachUsedBid(adInline);
                LogUtil.d("Adform-Banner", "onAdLoadSuccess");
            }

            @Override
            public void onAdLoadFail(AdInline adInline, String s) {
                LogUtil.d("Adform-Banner", "onAdLoadFail");
            }
        };
        
        return root;
    }

    private void setupBannerWithoutWait() {
        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adInline = new AdInline(getActivity());
        adInline.setMasterTagId(Constants.ADFORM_BANNER_MASTER_TAG);
        adInline.setEnabledAdditionalDimensions(true);
        adInline.setAdSize(new AdSize(320, 250));
        adInline.setListener(adListener);
        adFrame.addView(adInline);

        Prebid.attachBids(adInline, Constants.BANNER_300x250, this.getActivity());
        //endregion
        adInline.loadAd();

        adInline.onResume();
    }


    private void setupBannerWithWait(final int waitTime) {

        FrameLayout adFrame = (FrameLayout) root.findViewById(R.id.adFrame2);
        adFrame.removeAllViews();
        adInline2 = new AdInline(getActivity());
        adInline2.setMasterTagId(Constants.ADFORM_BANNER_MASTER_TAG);
        adInline2.setEnabledAdditionalDimensions(true);
        adInline2.setAdSize(new AdSize(320, 250));
        adInline2.setListener(adListener);
        adFrame.addView(adInline2);

        adInline2.onResume();

        Prebid.attachBidsWhenReady(adInline2, Constants.BANNER_300x250, this, waitTime, this.getActivity());
        //endregion
    }

    public void loadBanner() {
        if (adInline != null) {
            adInline.destroy();
        }
        if (adInline2 != null) {
            adInline2.destroy();
        }
        setupBannerWithoutWait();
        setupBannerWithWait(500);
    }

    @Override
    public void onAttachComplete(Object adObj) {
        if (adInline2 != null && adObj != null && adObj instanceof AdInline) {
            adInline2.loadAd();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adInline.onResume();
        adInline2.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        adInline.onPause();
        adInline2.onPause();
    }
}
