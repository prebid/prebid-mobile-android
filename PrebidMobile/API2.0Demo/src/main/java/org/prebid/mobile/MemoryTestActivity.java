package org.prebid.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

public class MemoryTestActivity extends Activity {
    int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorytest);
        final Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
                adFrame.removeAllViews();
                final PublisherAdView dfpAdView = new PublisherAdView(MemoryTestActivity.this);
                dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_300x250);
                dfpAdView.setAdSizes(new com.google.android.gms.ads.AdSize(300, 250));
                adFrame.addView(dfpAdView);
                dfpAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        LogUtil.d("ad failed " + count);
                        count++;
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        LogUtil.d("ad loaded " + count);
                        count++;
                    }
                });
                final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
                final PublisherAdRequest request = builder.build();
                //region Prebid Mobile API 2.0 usage
                final BannerAdUnit adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND);
                adUnit.addSize(300, 250);
                adUnit.fetchDemand(request, MemoryTestActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        if (request.getCustomTargeting().getString("hb_pb") != null) {
                            LogUtil.d("demand attached");
                        }
                        dfpAdView.loadAd(request);
                    }
                });
                mainThread.postDelayed(this, 10000);
            }
        });

    }

}
