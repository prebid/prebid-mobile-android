package org.prebid.mobile.app;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import org.prebid.mobile.*;

import java.util.ArrayList;

public class XandrNativeInBannerMoPubDemoActivity extends AppCompatActivity {
    AdUnit adUnit;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);
        adUnit = new NativeAdUnit(Constants.PBS_CONFIG_ID_NATIVE_APPNEXUS);
        final FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        final MoPubView adView = new MoPubView(this);
        adView.setAdUnitId(Constants.MOPUB_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS);
        adView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(final MoPubView banner) {
                adFrame.addView(banner);
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                LogUtil.debug("Banner failed " + errorCode);
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
        });
        adView.setAutorefreshEnabled(false);
        NativeAdUnit nativeAdUnit = (NativeAdUnit) adUnit;
        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
        try {
            NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            nativeAdUnit.addEventTracker(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(90);
        title.setRequired(true);
        nativeAdUnit.addAsset(title);
        NativeImageAsset icon = new NativeImageAsset(20, 20, 20, 20);
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setRequired(true);
        nativeAdUnit.addAsset(icon);
        NativeImageAsset image = new NativeImageAsset(200, 200, 200, 200);
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setRequired(true);
        nativeAdUnit.addAsset(image);
        NativeDataAsset data = new NativeDataAsset();
        data.setLen(90);
        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        data.setRequired(true);
        nativeAdUnit.addAsset(data);
        NativeDataAsset body = new NativeDataAsset();
        body.setRequired(true);
        body.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        nativeAdUnit.addAsset(body);
        NativeDataAsset cta = new NativeDataAsset();
        cta.setRequired(true);
        cta.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        nativeAdUnit.addAsset(cta);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        nativeAdUnit.setAutoRefreshPeriodMillis(millis);
        nativeAdUnit.fetchDemand(adView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                adView.loadAd(MoPubView.MoPubAdSize.MATCH_VIEW);
            }
        });
    }
}
