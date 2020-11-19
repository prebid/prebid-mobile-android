package org.prebid.mobile.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.StaticNativeAd;

import org.prebid.mobile.CacheManager;
import org.prebid.mobile.Host;
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
import org.prebid.mobile.PrebidServerAdapter;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;

import java.util.ArrayList;
import java.util.Map;

public class PrebidNativeActivity extends AppCompatActivity {
    private PublisherAdView adView;
    private MoPubNative mMoPubNative;
    private AdLoader adLoader;
    private UnifiedNativeAd unifiedNativeAd;
    private NativeAd ad;

    private void removePreviousAds() {
        ((FrameLayout) findViewById(R.id.adFrame)).removeAllViews();
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        if (ad != null) {
            ad.destroy();
            ad = null;
        }
        if (mMoPubNative != null) {
            mMoPubNative.destroy();
            mMoPubNative = null;
        }
        if (unifiedNativeAd != null) {
            unifiedNativeAd.destroy();
            unifiedNativeAd = null;
        }

    }

    private void infalteMoPubNativeAd(NativeAd nativeAd) {
        Log.d("Prebid", "came here");
        final StaticNativeAd ad = (StaticNativeAd) nativeAd.getBaseNativeAd();
        Log.d("Prebid", ""+ad.getExtras().toString());
        LinearLayout nativeContainer = new LinearLayout(PrebidNativeActivity.this);
        nativeContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout iconAndTitle = new LinearLayout(PrebidNativeActivity.this);
        iconAndTitle.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(PrebidNativeActivity.this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
        Util.loadImage(icon, ad.getIconImageUrl());
        iconAndTitle.addView(icon);
        TextView title = new TextView(PrebidNativeActivity.this);
        title.setTextSize(20);
        title.setText(ad.getTitle());
        iconAndTitle.addView(title);
        nativeContainer.addView(iconAndTitle);
        ImageView image = new ImageView(PrebidNativeActivity.this);
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Util.loadImage(image, ad.getMainImageUrl());
        nativeContainer.addView(image);
        TextView description = new TextView(PrebidNativeActivity.this);
        description.setTextSize(18);
        description.setText(ad.getText());
        nativeContainer.addView(description);
        Button cta = new Button(PrebidNativeActivity.this);
        cta.setText(ad.getCallToAction());
        cta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getClickDestinationUrl()));
                startActivity(browserIntent);
            }
        });
        nativeContainer.addView(cta);
        ((FrameLayout) PrebidNativeActivity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
    }

    private void inflatePrebidNativeAd(final PrebidNativeAd ad) {
        LinearLayout nativeContainer = new LinearLayout(PrebidNativeActivity.this);
        ad.registerView(nativeContainer, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                Toast.makeText(PrebidNativeActivity.this, "onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(PrebidNativeActivity.this, "onAdImpression", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdExpired() {
                Toast.makeText(PrebidNativeActivity.this, "onAdExpired", Toast.LENGTH_SHORT).show();
            }
        });
        nativeContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout iconAndTitle = new LinearLayout(PrebidNativeActivity.this);
        iconAndTitle.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(PrebidNativeActivity.this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
//        Util.loadImage(icon, ad.getIconUrl());
        iconAndTitle.addView(icon);
        TextView title = new TextView(PrebidNativeActivity.this);
        title.setTextSize(20);
        title.setText(ad.getTitle());
        iconAndTitle.addView(title);
        nativeContainer.addView(iconAndTitle);
        ImageView image = new ImageView(PrebidNativeActivity.this);
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Util.loadImage(image, ad.getImageUrl());
        nativeContainer.addView(image);
        TextView description = new TextView(PrebidNativeActivity.this);
        description.setTextSize(18);
//        description.setText(ad.getDescription());
        nativeContainer.addView(description);
        Button cta = new Button(PrebidNativeActivity.this);
        cta.setText(ad.getCallToAction());
        nativeContainer.addView(cta);
        ((FrameLayout) PrebidNativeActivity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prebid_native);
        SdkConfiguration sdkConfiguration = new SdkConfiguration
                .Builder("2674981035164b2db5ef4b4546bf3d49")
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .build();
        MoPub.initializeSdk(this, sdkConfiguration, null);
    }

    public void loadNativeNative(View view) {
        removePreviousAds();
        PrebidServerAdapter.testingNativeNative = true;
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
//                    .addCustomTargeting("hb_pb", "0.80")
                    .addCustomTargeting("hb_pb", "0.00")
                .build();
        nativeAdUnit.fetchDemand(publisherAdRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                if (resultCode == ResultCode.SUCCESS) {
                    Toast.makeText(PrebidNativeActivity.this, "Native Ad Unit: " + publisherAdRequest.getKeywords() + ", Custom Targeting: " + publisherAdRequest.getCustomTargeting(), Toast.LENGTH_SHORT).show();
                    Log.e("Custom Targeting: ", publisherAdRequest.getCustomTargeting().toString());
                    loadDfp(publisherAdRequest);
                } else {
                    Toast.makeText(PrebidNativeActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadDfp(PublisherAdRequest publisherAdRequest) {
        adLoader = new AdLoader.Builder(this, "/19968336/Abhas_test_native_native_adunit")
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
                        Log.d("Prebid", "native loaded");
                        PrebidNativeActivity.this.unifiedNativeAd = unifiedNativeAd;
                    }
                })
                .forCustomTemplateAd("11963183", new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {

                    @Override
                    public void onCustomTemplateAdLoaded(NativeCustomTemplateAd nativeCustomTemplateAd) {
                        Log.d("Prebid", "custom ad loaded");
                        Util.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
                            @Override
                            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                                inflatePrebidNativeAd(ad);
                            }

                            @Override
                            public void onPrebidNativeNotFound() {
                                Log.e("Prebid", "onPrebidNativeNotFound");
                                // inflate nativeCustomTemplateAd
                            }

                            @Override
                            public void onPrebidNativeNotValid() {
                                Log.e("Prebid", "onPrebidNativeNotFound");
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
                        Toast.makeText(PrebidNativeActivity.this, "DFP onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        adLoader.loadAd(publisherAdRequest);
    }

    // Mopub
    public void loadNativeNativeMopub(View v) {
        {
            removePreviousAds();
            PrebidServerAdapter.testingNativeNative = true;
            PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
            PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);

            final NativeAdUnit nativeAdUnit = new NativeAdUnit(Constants.PBS_CONFIG_ID_NATIVE_APPNEXUS);
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

            mMoPubNative = new MoPubNative(PrebidNativeActivity.this, "2674981035164b2db5ef4b4546bf3d49", new MoPubNative.MoPubNativeNetworkListener() {
                @Override
                public void onNativeLoad(final NativeAd nativeAd) {
                    Log.d("Prebid", "MoPub native ad loaded");
                    PrebidNativeActivity.this.ad = nativeAd;
                    Util.findNative(nativeAd, new PrebidNativeAdListener() {
                        @Override
                        public void onPrebidNativeLoaded(final PrebidNativeAd ad) {
                            inflatePrebidNativeAd(ad);
                        }

                        @Override
                        public void onPrebidNativeNotFound() {
                            infalteMoPubNativeAd(nativeAd);
                        }

                        @Override
                        public void onPrebidNativeNotValid() {
                            Log.e("ERROR", "onPrebidNativeNotValid");
                            // should not show the NativeAd on the screen, do something else
                        }
                    });

                }

                @Override
                public void onNativeFail(NativeErrorCode errorCode) {
                    Log.d("Prebid", "MoPub native failed to load: " + errorCode.toString());
                }
            });
            mMoPubNative.registerAdRenderer(new MoPubStaticNativeAdRenderer(null));
            String keywords = "hb_pb:0.50";
            RequestParameters mRP = new RequestParameters.Builder().keywords(keywords).build();
            nativeAdUnit.fetchDemand(mRP, new OnCompleteListener() {
                @Override
                public void onComplete(ResultCode resultCode) {
                    Toast.makeText(PrebidNativeActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                    Log.e("Prebid", mRP.getKeywords());
                    mMoPubNative.makeRequest(mRP);
                }
            });

            // Fetchingn the demannd using OnCompleteListener2
//            nativeAdUnit.fetchDemand(new OnCompleteListener2() {
//                @Override
//                public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
//                    Log.e("MAP", unmodifiableMap.toString());
//                    if (resultCode == ResultCode.SUCCESS) {
//                        String keywords = "hb_pb:0.50";
//                        if (unmodifiableMap.containsKey("hb_cache_id_local"))
//                            keywords += ",hb_cache_id:" + unmodifiableMap.get("hb_cache_id_local");
//                        RequestParameters mRP = new RequestParameters.Builder().keywords(keywords).build();
//                        Log.d("Prebid", mRP.getKeywords());
//                        mMoPubNative.makeRequest(mRP);
//                    }
//                    Toast.makeText(PrebidNativeActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }


}
