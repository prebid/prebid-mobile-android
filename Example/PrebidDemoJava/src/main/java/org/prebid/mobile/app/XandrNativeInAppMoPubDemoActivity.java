package org.prebid.mobile.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.nativeads.*;
import org.prebid.mobile.Util;
import org.prebid.mobile.*;
import org.prebid.mobile.addendum.AdViewUtils;

import java.util.ArrayList;

public class XandrNativeInAppMoPubDemoActivity extends AppCompatActivity {
    private MoPubNative moPubNative;
    private NativeAd ad;

    //Used by UI tests
    int refreshCount;
    ResultCode resultCode;
    RequestParameters request;
    AdUnit adUnit;

    private void removePreviousAds() {
        ((FrameLayout) findViewById(R.id.adFrame)).removeAllViews();
        if (ad != null) {
            ad.destroy();
            ad = null;
        }
        if (moPubNative != null) {
            moPubNative.destroy();
            moPubNative = null;
        }
    }

    private void inflatePrebidNativeAd(final PrebidNativeAd ad) {
        View nativeContainer = View.inflate(this, R.layout.layout_native, null);
        ad.registerView(nativeContainer, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                Toast.makeText(XandrNativeInAppMoPubDemoActivity.this, "onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(XandrNativeInAppMoPubDemoActivity.this, "onAdImpression", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdExpired() {
                Toast.makeText(XandrNativeInAppMoPubDemoActivity.this, "onAdExpired", Toast.LENGTH_SHORT).show();
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
        ((FrameLayout) XandrNativeInAppMoPubDemoActivity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
    }


    private void inflateMoPubNativeAd(NativeAd nativeAd) {
        Log.d("Prebid", "came here");
        final StaticNativeAd ad = (StaticNativeAd) nativeAd.getBaseNativeAd();
        Log.d("Prebid", ""+ad.getExtras().toString());
        LinearLayout nativeContainer = new LinearLayout(XandrNativeInAppMoPubDemoActivity.this);
        nativeContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout iconAndTitle = new LinearLayout(XandrNativeInAppMoPubDemoActivity.this);
        iconAndTitle.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(XandrNativeInAppMoPubDemoActivity.this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
        Util.loadImage(icon, ad.getIconImageUrl());
        iconAndTitle.addView(icon);
        TextView title = new TextView(XandrNativeInAppMoPubDemoActivity.this);
        title.setTextSize(20);
        title.setText(ad.getTitle());
        iconAndTitle.addView(title);
        nativeContainer.addView(iconAndTitle);
        ImageView image = new ImageView(XandrNativeInAppMoPubDemoActivity.this);
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Util.loadImage(image, ad.getMainImageUrl());
        nativeContainer.addView(image);
        TextView description = new TextView(XandrNativeInAppMoPubDemoActivity.this);
        description.setTextSize(18);
        description.setText(ad.getText());
        nativeContainer.addView(description);
        Button cta = new Button(XandrNativeInAppMoPubDemoActivity.this);
        cta.setText(ad.getCallToAction());
        cta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getClickDestinationUrl()));
                startActivity(browserIntent);
            }
        });
        nativeContainer.addView(cta);
        ((FrameLayout) XandrNativeInAppMoPubDemoActivity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        SdkConfiguration sdkConfiguration = new SdkConfiguration
                .Builder("2674981035164b2db5ef4b4546bf3d49")
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .build();
        MoPub.initializeSdk(this, sdkConfiguration, null);
        loadInAppNativeMopub();
    }

    // Mopub
    private void loadInAppNativeMopub() {
        {
            removePreviousAds();
            PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
            PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);

            final NativeAdUnit nativeAdUnit = new NativeAdUnit(Constants.PBS_CONFIG_ID_NATIVE_APPNEXUS);
            nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
            nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
            nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
            adUnit = nativeAdUnit;
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

            moPubNative = new MoPubNative(XandrNativeInAppMoPubDemoActivity.this, "2674981035164b2db5ef4b4546bf3d49", new MoPubNative.MoPubNativeNetworkListener() {
                @Override
                public void onNativeLoad(final NativeAd nativeAd) {
                    Log.d("Prebid", "MoPub native ad loaded");
                    XandrNativeInAppMoPubDemoActivity.this.ad = nativeAd;
                    AdViewUtils.findNative(nativeAd, new PrebidNativeAdListener() {
                        @Override
                        public void onPrebidNativeLoaded(final PrebidNativeAd ad) {
                            inflatePrebidNativeAd(ad);
                        }

                        @Override
                        public void onPrebidNativeNotFound() {
                            inflateMoPubNativeAd(nativeAd);
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
            moPubNative.registerAdRenderer(new MoPubStaticNativeAdRenderer(null));
            RequestParameters.Builder requestParams = new RequestParameters.Builder();

            // Fetching the demannd using OnCompleteListener
            nativeAdUnit.fetchDemand(requestParams, new OnCompleteListener() {
                @Override
                public void onComplete(ResultCode resultCode) {
                    if (resultCode == ResultCode.SUCCESS) {
                        moPubNative.makeRequest(requestParams.build());
                    } else {
                        Toast.makeText(
                                XandrNativeInAppMoPubDemoActivity.this,
                                "Native Ad Unit: " + resultCode.name(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    refreshCount++;
                    XandrNativeInAppMoPubDemoActivity.this.resultCode = resultCode;
                    request = requestParams.build();
                }
            });

            //================================================================================
            // SAMPLE CODE: Fetching the demand using OnCompleteListener2
            //================================================================================

            /*
            nativeAdUnit.fetchDemand(new OnCompleteListener2() {
                @Override
                public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                    Log.e("MAP", unmodifiableMap.toString());
                    if (resultCode == ResultCode.SUCCESS) {
                        String keywords = "";
                        for (String key: unmodifiableMap.keySet()) {
                            keywords += key + ":" + unmodifiableMap.get(key) + ",";
                        }
                        // removing last ","
                        keywords = keywords.substring(0, keywords.length()-1);
                        RequestParameters requestParams = new RequestParameters.Builder().keywords(keywords).build();
                        Log.d("Prebid", requestParams.getKeywords());
                        moPubNative.makeRequest(requestParams);
                    }
                    Toast.makeText(XandrNativeInAppMoPubDemoActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                }
            });
            */

            //================================================================================
            // SAMPLE CODE: END
            //================================================================================
        }
    }


}
