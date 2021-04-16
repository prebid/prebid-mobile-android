package org.prebid.mobile.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import org.prebid.mobile.Host;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;
import org.prebid.mobile.PrebidNativeAdListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;
import org.prebid.mobile.addendum.AdViewUtils;

import java.util.ArrayList;

import static org.prebid.mobile.app.Constants.DFP_NATIVE_NATIVE_ADUNIT_ID_APPNEXUS;

public class XandrNativeInAppGAMDemoActivity extends AppCompatActivity {
    private AdManagerAdView adView;
    private AdLoader adLoader;
    private UnifiedNativeAd unifiedNativeAd;

    private void removePreviousAds() {
        ((FrameLayout) findViewById(R.id.adFrame)).removeAllViews();
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        if (unifiedNativeAd != null) {
            unifiedNativeAd.destroy();
            unifiedNativeAd = null;
        }

    }

    private void inflatePrebidNativeAd(final PrebidNativeAd ad) {
        View nativeContainer = View.inflate(this, R.layout.layout_native, null);
        ad.registerView(nativeContainer, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "onAdImpression", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdExpired() {
                Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "onAdExpired", Toast.LENGTH_SHORT).show();
            }
        });
        ImageView icon = nativeContainer.findViewById(R.id.imgIcon);
        Util.loadImage(icon, ad.getIconUrl());
        TextView title = nativeContainer.findViewById(R.id.tvTitle);
        title.setText(ad.getTitle());
        ImageView image = nativeContainer.findViewById(R.id.imgImage);
        Util.loadImage(image, ad.getImageUrl());
        TextView description = nativeContainer.findViewById(R.id.tvDesc);
        description.setText(ad.getDescription());
        Button cta = nativeContainer.findViewById(R.id.btnCta);
        cta.setText(ad.getCallToAction());
        ((FrameLayout) XandrNativeInAppGAMDemoActivity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        loadInAppNative();
    }

    private void loadInAppNative() {
        removePreviousAds();
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);

        NativeAdUnit nativeAdUnit = new NativeAdUnit(Constants.PBS_CONFIG_ID_NATIVE_APPNEXUS);
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
        NativeImageAsset icon = new NativeImageAsset();
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setWMin(20);
        icon.setHMin(20);
        icon.setRequired(true);
        nativeAdUnit.addAsset(icon);
        NativeImageAsset image = new NativeImageAsset();
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setHMin(200);
        image.setWMin(200);
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

        final AdManagerAdRequest adManagerAdRequest = new AdManagerAdRequest.Builder()
                .build();

        // Fetching the demannd using OnCompleteListener
        nativeAdUnit.fetchDemand(adManagerAdRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                if (resultCode == ResultCode.SUCCESS) {
                    loadDfp(adManagerAdRequest);
                } else {
                    Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //================================================================================
        // SAMPLE CODE: Fetching the demand using OnCompleteListener2
        //================================================================================

        /*
        nativeAdUnit.fetchDemand(new OnCompleteListener2() {
            @Override
            public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                if (resultCode == ResultCode.SUCCESS) {
                    final AdManagerAdRequest.Builder adManagerAdRequestBuilder = new AdManagerAdRequest.Builder();
                    for (String key: unmodifiableMap.keySet()) {
                        adManagerAdRequestBuilder.addCustomTargeting(key, unmodifiableMap.get(key));
                    }
                    loadDfp(adManagerAdRequestBuilder.build());
                }
                Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
            }
        });
        */

        //================================================================================
        // SAMPLE CODE: END
        //================================================================================

    }

    private void loadDfp(AdManagerAdRequest adManagerAdRequest) {
        adLoader = new AdLoader.Builder(this, DFP_NATIVE_NATIVE_ADUNIT_ID_APPNEXUS)
                .forAdManagerAdView(new OnAdManagerAdViewLoadedListener() {
                    @Override
                    public void onAdManagerAdViewLoaded(AdManagerAdView adManagerAdView) {
                        adView = adManagerAdView;
                        ((FrameLayout) findViewById(R.id.adFrame)).addView(adManagerAdView);
                    }
                }, AdSize.BANNER)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        LogUtil.d("Prebid", "native loaded");
                        XandrNativeInAppGAMDemoActivity.this.unifiedNativeAd = unifiedNativeAd;
                    }
                })
                .forCustomTemplateAd("11963183", new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {

                    @Override
                    public void onCustomTemplateAdLoaded(NativeCustomTemplateAd nativeCustomTemplateAd) {
                        LogUtil.d("Prebid", "custom ad loaded");
                        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
                            @Override
                            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                                inflatePrebidNativeAd(ad);
                            }

                            @Override
                            public void onPrebidNativeNotFound() {
                                LogUtil.d("Prebid", "onPrebidNativeNotFound");
                                // inflate nativeCustomTemplateAd
                            }

                            @Override
                            public void onPrebidNativeNotValid() {
                                LogUtil.d("Prebid", "onPrebidNativeNotFound");
                                // show your own content
                            }
                        });
                    }
                }, new NativeCustomTemplateAd.OnCustomClickListener() {
                    @Override
                    public void onCustomClick(NativeCustomTemplateAd nativeCustomTemplateAd, String s) {

                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "DFP onAdFailedToLoad", Toast.LENGTH_SHORT).show();

                    }
                })
                .build();

        adLoader.loadAd(adManagerAdRequest);
    }
}
