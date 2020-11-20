package org.prebid.mobile.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.logging.MoPubLog;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.StaticNativeAd;

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
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;

import java.util.ArrayList;
import java.util.Map;

public class XandrNativeInAppMoPubDemo2Activity extends AppCompatActivity {
    private MoPubNative mMoPubNative;
    private NativeAd ad;

    private void removePreviousAds() {
        ((FrameLayout) findViewById(R.id.adFrame)).removeAllViews();
        if (ad != null) {
            ad.destroy();
            ad = null;
        }
        if (mMoPubNative != null) {
            mMoPubNative.destroy();
            mMoPubNative = null;
        }
    }

    private void inflateMoPubNativeAd(NativeAd nativeAd) {
        Log.d("Prebid", "came here");
        final StaticNativeAd ad = (StaticNativeAd) nativeAd.getBaseNativeAd();
        Log.d("Prebid", ""+ad.getExtras().toString());
        LinearLayout nativeContainer = new LinearLayout(XandrNativeInAppMoPubDemo2Activity.this);
        nativeContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout iconAndTitle = new LinearLayout(XandrNativeInAppMoPubDemo2Activity.this);
        iconAndTitle.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(XandrNativeInAppMoPubDemo2Activity.this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
        Util.loadImage(icon, ad.getIconImageUrl());
        iconAndTitle.addView(icon);
        TextView title = new TextView(XandrNativeInAppMoPubDemo2Activity.this);
        title.setTextSize(20);
        title.setText(ad.getTitle());
        iconAndTitle.addView(title);
        nativeContainer.addView(iconAndTitle);
        ImageView image = new ImageView(XandrNativeInAppMoPubDemo2Activity.this);
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Util.loadImage(image, ad.getMainImageUrl());
        nativeContainer.addView(image);
        TextView description = new TextView(XandrNativeInAppMoPubDemo2Activity.this);
        description.setTextSize(18);
        description.setText(ad.getText());
        nativeContainer.addView(description);
        Button cta = new Button(XandrNativeInAppMoPubDemo2Activity.this);
        cta.setText(ad.getCallToAction());
        cta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getClickDestinationUrl()));
                startActivity(browserIntent);
            }
        });
        nativeContainer.addView(cta);
        ((FrameLayout) XandrNativeInAppMoPubDemo2Activity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
    }

    private void inflatePrebidNativeAd(final PrebidNativeAd ad) {
        LinearLayout nativeContainer = new LinearLayout(XandrNativeInAppMoPubDemo2Activity.this);
        ad.registerView(nativeContainer, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                Toast.makeText(XandrNativeInAppMoPubDemo2Activity.this, "onAdClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdImpression() {
                Toast.makeText(XandrNativeInAppMoPubDemo2Activity.this, "onAdImpression", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdExpired() {
                Toast.makeText(XandrNativeInAppMoPubDemo2Activity.this, "onAdExpired", Toast.LENGTH_SHORT).show();
            }
        });
        nativeContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout iconAndTitle = new LinearLayout(XandrNativeInAppMoPubDemo2Activity.this);
        iconAndTitle.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(XandrNativeInAppMoPubDemo2Activity.this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
//        Util.loadImage(icon, ad.getIconUrl());
        iconAndTitle.addView(icon);
        TextView title = new TextView(XandrNativeInAppMoPubDemo2Activity.this);
        title.setTextSize(20);
        title.setText(ad.getTitle());
        iconAndTitle.addView(title);
        nativeContainer.addView(iconAndTitle);
        ImageView image = new ImageView(XandrNativeInAppMoPubDemo2Activity.this);
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Util.loadImage(image, ad.getImageUrl());
        nativeContainer.addView(image);
        TextView description = new TextView(XandrNativeInAppMoPubDemo2Activity.this);
        description.setTextSize(18);
        description.setText(ad.getDescription());
        nativeContainer.addView(description);
        Button cta = new Button(XandrNativeInAppMoPubDemo2Activity.this);
        cta.setText(ad.getCallToAction());
        nativeContainer.addView(cta);
        ((FrameLayout) XandrNativeInAppMoPubDemo2Activity.this.findViewById(R.id.adFrame)).addView(nativeContainer);
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
        loadNativeNativeMopub();
    }

    // Mopub
    private void loadNativeNativeMopub() {
        {
            removePreviousAds();
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

            mMoPubNative = new MoPubNative(XandrNativeInAppMoPubDemo2Activity.this, "2674981035164b2db5ef4b4546bf3d49", new MoPubNative.MoPubNativeNetworkListener() {
                @Override
                public void onNativeLoad(final NativeAd nativeAd) {
                    Log.d("Prebid", "MoPub native ad loaded");
                    XandrNativeInAppMoPubDemo2Activity.this.ad = nativeAd;
                    Util.findNative(nativeAd, new PrebidNativeAdListener() {
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
            mMoPubNative.registerAdRenderer(new MoPubStaticNativeAdRenderer(null));
            // Fetching the demand using OnCompleteListener2
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
                        RequestParameters mRP = new RequestParameters.Builder().keywords(keywords).build();
                        Log.d("Prebid", mRP.getKeywords());
                        mMoPubNative.makeRequest(mRP);
                    }
                    Toast.makeText(XandrNativeInAppMoPubDemo2Activity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
