package org.prebid.mobile.app;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;
import com.google.android.gms.ads.formats.OnPublisherAdViewLoadedListener;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.mopub.nativeads.RequestParameters;

import org.prebid.mobile.Host;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.OnCompleteListener2;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;
import org.prebid.mobile.PrebidNativeAdListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;

import java.util.ArrayList;
import java.util.Map;

import static org.prebid.mobile.app.Constants.DFP_NATIVE_NATIVE_ADUNIT_ID_APPNEXUS;

public class XandrNativeInAppGAMDemoActivity extends AppCompatActivity {
    private PublisherAdView adView;
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
        LinearLayout nativeContainer = new LinearLayout(XandrNativeInAppGAMDemoActivity.this);
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
        nativeContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout iconAndTitle = new LinearLayout(XandrNativeInAppGAMDemoActivity.this);
        iconAndTitle.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(XandrNativeInAppGAMDemoActivity.this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
        Util.loadImage(icon, ad.getIconUrl());
        iconAndTitle.addView(icon);
        TextView title = new TextView(XandrNativeInAppGAMDemoActivity.this);
        title.setTextSize(20);
        title.setText(ad.getTitle());
        iconAndTitle.addView(title);
        nativeContainer.addView(iconAndTitle);
        ImageView image = new ImageView(XandrNativeInAppGAMDemoActivity.this);
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Util.loadImage(image, ad.getImageUrl());
        nativeContainer.addView(image);
        TextView description = new TextView(XandrNativeInAppGAMDemoActivity.this);
        description.setTextSize(18);
        description.setText(ad.getDescription());
        nativeContainer.addView(description);
        Button cta = new Button(XandrNativeInAppGAMDemoActivity.this);
        cta.setText(ad.getCallToAction());
        nativeContainer.addView(cta);
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

        final PublisherAdRequest publisherAdRequest = new PublisherAdRequest.Builder()
                .build();

        // Fetching the demannd using OnCompleteListener
        nativeAdUnit.fetchDemand(publisherAdRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                if (resultCode == ResultCode.SUCCESS) {
                    loadDfp(publisherAdRequest);
                } else {
                    Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //================================================================================
        // SAMPLE CODE: Fetching the demand using OnCompleteListener2
        //================================================================================

        /*nativeAdUnit.fetchDemand(new OnCompleteListener2() {
            @Override
            public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                if (resultCode == ResultCode.SUCCESS) {
                    final PublisherAdRequest.Builder publisherAdRequestBuilder = new PublisherAdRequest.Builder();
                    for (String key: unmodifiableMap.keySet()) {
                        publisherAdRequestBuilder.addCustomTargeting(key, unmodifiableMap.get(key));
                    }
                    loadDfp(publisherAdRequestBuilder.build());
                }
                Toast.makeText(XandrNativeInAppGAMDemo2Activity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
            }
        });*/

        //================================================================================
        // SAMPLE CODE: END
        //================================================================================

    }

    private void loadDfp(PublisherAdRequest publisherAdRequest) {
        adLoader = new AdLoader.Builder(this, DFP_NATIVE_NATIVE_ADUNIT_ID_APPNEXUS)
                .forPublisherAdView(new OnPublisherAdViewLoadedListener() {
                    @Override
                    public void onPublisherAdViewLoaded(PublisherAdView publisherAdView) {
                        adView = publisherAdView;
                        ((FrameLayout) findViewById(R.id.adFrame)).addView(publisherAdView);
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
                        Util.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
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
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        Toast.makeText(XandrNativeInAppGAMDemoActivity.this, "DFP onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        adLoader.loadAd(publisherAdRequest);
    }
}
