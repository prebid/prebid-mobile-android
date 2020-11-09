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
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;
import org.prebid.mobile.PrebidNativeAdListener;
import org.prebid.mobile.PrebidServerAdapter;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;

import java.util.ArrayList;

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

    private void loadDFPCustomRendering(boolean usePrebid) {
        removePreviousAds();
        adLoader = new AdLoader.Builder(this, "/19968336/Wei_test_native_native")
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
                .forCustomTemplateAd("11885766", new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {

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
                                // inflate nativeCustomTemplateAd
                            }

                            @Override
                            public void onPrebidNativeNotValid() {
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
                    }
                })
                .build();
        PublisherAdRequest request;
        String cacheId = CacheManager.save("{\n" +
                "  \"ver\": \"1.2\",\n" +
                "  \"assets\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"img\": {\n" +
                "        \"type\": 3,\n" +
                "        \"url\": \"https://vcdn.adnxs.com/p/creative-image/7e/71/90/27/7e719027-80ef-4664-9b6d-a763da4cea4e.png\",\n" +
                "        \"w\": 300,\n" +
                "        \"h\": 250,\n" +
                "        \"ext\": {\n" +
                "          \"appnexus\": {\n" +
                "            \"prevent_crop\": 0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"data\": {\n" +
                "        \"type\": 1,\n" +
                "        \"value\": \"AppNexus\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"title\": {\n" +
                "        \"text\": \"This is an RTB ad\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"link\": {\n" +
                "    \"url\": \"https://nym1-ib.adnxs.com/click?mpmZmZmZqT-amZmZmZmpPwAAAAAAAOA_mpmZmZmZqT-amZmZmZmpP8GRp4bdjMle__________8UQVpfAAAAAHu99gBuJwAAbicAAAIAAACRL6wJ-MwcAAAAAABVU0QAVVNEAAEAAQDILwAAAAABAgMCAAAAAMYAfyvwMQAAAAA./bcr=AAAAAAAA8D8=/pp=${AUCTION_PRICE}/cnd=%211BKZHAiFzYATEJHfsE0Y-JlzIAQoADGamZmZmZmpPzoJTllNMjo0MDU2QKYkSQAAAAAAAPA_UQAAAAAAAAAAWQAAAAAAAAAAYQAAAAAAAAAAaQAAAAAAAAAAcQAAAAAAAAAAeAA./cca=MTAwOTQjTllNMjo0MDU2/bn=77455/clickenc=http%3A%2F%2Fappnexus.com\"\n" +
                "  },\n" +
                "  \"eventtrackers\": [\n" +
                "    {\n" +
                "      \"event\": 1,\n" +
                "      \"method\": 1,\n" +
                "      \"url\": \"https://nym1-ib.adnxs.com/it?an_audit=0&e=wqT_3QLhCWzhBAAAAwDWAAUBCJSC6foFEMGjnrXYm-PkXhj_EQEUASo2CZqZAQEIqT8REQkEGQAFAQjgPyEREgApEQkAMQUauADgPzD7-toHOO5OQO5OSAJQkd-wTVj4mXNgAGjI34wBeI_dBIABAYoBA1VTRJIBAQbwVZgBAaABAagBAbABALgBAsABA8gBAtABANgBAOABAPABAIoCPHVmKCdhJywgMzM5MzUyMCwgMTU5OTc1MDQyMCk7dWYoJ3InLCAxNjIyNzkzMTMsIDE1GR_0DgGSAvkDIWdGazdGZ2lGellBVEVKSGZzRTBZQUNENG1YTXdBRGdBUUFSSTdrNVEtX3JhQjFnQVlQX19fXzhQYUFCd0FYZ0JnQUVCaUFFQmtBRUJtQUVCb0FFQnFBRURzQUVBdVFGMXF3MXNtcG1wUDhFQmRhc05iSnFacVRfSkFYd2xrdlZtdVBBXzJRRUFBQUFBQUFEd1AtQUJBUFVCQUFBQUFKZ0NBS0FDQUxVQ0FBQUFBTDBDQUFBQUFNQUNBY2dDQWRBQ0FkZ0NBZUFDQU9nQ0FQZ0NBSUFEQVpnREFhZ0RoYzJBRTdvRENVNVpUVEk2TkRBMU51QURwaVNJQkFDUUJBQ1lCQUhCQkFBQUFBCYMIeVFRCQkBARhOZ0VBUEVFAQsJASBDSUJkZ2ZxUVUJDxhBRHdQN0VGDQ0UQUFBREJCHT8AeRUoDEFBQU4yKAAAWi4oAPBANEFXSUpfQUY0djN4QV9nRjhJX1BBWUlHQTFWVFJJZ0dBSkFHQVpnR0FLRUdtcG1abVptWnFULW9CZ0d5QmlRSkEBYAkBAFIJBwUBAFoFBgkBAGgJBwEBQEM0QmdvLpoCiQEhMUJLWkhBNv0BMC1KbHpJQVFvQURHYW0Fa1htcFB6b0pUbGxOTWpvME1EVTJRS1lrUxHpDFBBX1URDAxBQUFXHQwAWR0MAGEdDABjHQzwpGVBQS7YAgDgAsqoTYADAYgDAJADAJgDFKADAaoDAMAD4KgByAMA2AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAqAQAsgQMCAAQABgAIAAwADgAuAQAwAQAyAQA0gQPMTAwOTQjTllNMjo0MDU22gQCCAHgBADwBJHfsE2CBRpvcmcucHJlYmlkLm1vYmlsZS5hcGkxZGVtb4gFAZgFAKAF_3X8sKoFJDZhZjFlZmQ3LWJlNmMtNDlmMS04MTk2LWViNjI0NWI5ZWFjZsAFAMkFAGX5FPA_0gUJCQULOAAAANgFAeAFAfAFAfoFBAGwKJAGAZgGALgGAMEGAR8wAADwP9AG1jPaBhYKEAkRGQFcEAAYAOAGDPIGAggAgAcBiAcAoAdBugcOAUgEGAAJ-CRAAMgHj90E0gcNFXMwEAAYANoHBggAEAAYAA..&s=46d93f84d8459a2ca773485e5721255200b9f0ed&pp=${AUCTION_PRICE}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");
//        String cacheId = CacheManager.save("{\"title\":\"Test title\",\"description\":\"This is a test ad for Prebid Native Native. Please check prebid.org\",\"cta\":\"Learn More\",\"iconUrl\":\"https://dummyimage.com/40x40/000/fff\",\"imageUrl\":\"https://dummyimage.com/600x400/000/fff\",\"clickUrl\":\"https://prebid.org/\"}");
        if (usePrebid) {
            request = new PublisherAdRequest.Builder()
                    .addCustomTargeting("hb_pb", "0.80")
//                    .addCustomTargeting("hb_cache_id", cacheId)
                    .build();
        } else {
            request = new PublisherAdRequest.Builder().build();
        }

        adLoader.loadAd(request);
    }

    private void loadDFPNativeNative(boolean usePrebid) {
        removePreviousAds();
        adView = new PublisherAdView(this);
        adView.setAdUnitId("/19968336/Wei_test_native_native");
        adView.setAdSizes(AdSize.BANNER);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d("Prebid", "dfp failed to load " + i);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Util.findNative(adView, new PrebidNativeAdListener() {
                    @Override
                    public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                        // Display Native
                        Log.d("Prebid", "DFP Prebid Native ad loaded");
                        inflatePrebidNativeAd(ad);
                    }

                    @Override
                    public void onPrebidNativeNotFound() {
                        // Display Banner
                        Log.d("Prebid", "DFP Prebid Native not found, display regular banner");
                        ((FrameLayout) PrebidNativeActivity.this.findViewById(R.id.adFrame)).addView(adView);
                    }

                    @Override
                    public void onPrebidNativeNotValid() {
                        //should not show the NativeAd on the screen, do something else
                        Log.d("Prebid", "DFP Prebid Native not valid");
                    }
                });
            }
        });

        PublisherAdRequest request;
        String cacheId = CacheManager.save("{\n" +
                "  \"ver\": \"1.2\",\n" +
                "  \"assets\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"img\": {\n" +
                "        \"type\": 3,\n" +
                "        \"url\": \"https://vcdn.adnxs.com/p/creative-image/7e/71/90/27/7e719027-80ef-4664-9b6d-a763da4cea4e.png\",\n" +
                "        \"w\": 300,\n" +
                "        \"h\": 250,\n" +
                "        \"ext\": {\n" +
                "          \"appnexus\": {\n" +
                "            \"prevent_crop\": 0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"data\": {\n" +
                "        \"type\": 1,\n" +
                "        \"value\": \"AppNexus\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"title\": {\n" +
                "        \"text\": \"This is an RTB ad\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"link\": {\n" +
                "    \"url\": \"https://nym1-ib.adnxs.com/click?mpmZmZmZqT-amZmZmZmpPwAAAAAAAOA_mpmZmZmZqT-amZmZmZmpP8GRp4bdjMle__________8UQVpfAAAAAHu99gBuJwAAbicAAAIAAACRL6wJ-MwcAAAAAABVU0QAVVNEAAEAAQDILwAAAAABAgMCAAAAAMYAfyvwMQAAAAA./bcr=AAAAAAAA8D8=/pp=${AUCTION_PRICE}/cnd=%211BKZHAiFzYATEJHfsE0Y-JlzIAQoADGamZmZmZmpPzoJTllNMjo0MDU2QKYkSQAAAAAAAPA_UQAAAAAAAAAAWQAAAAAAAAAAYQAAAAAAAAAAaQAAAAAAAAAAcQAAAAAAAAAAeAA./cca=MTAwOTQjTllNMjo0MDU2/bn=77455/clickenc=http%3A%2F%2Fappnexus.com\"\n" +
                "  },\n" +
                "  \"eventtrackers\": [\n" +
                "    {\n" +
                "      \"event\": 1,\n" +
                "      \"method\": 1,\n" +
                "      \"url\": \"https://nym1-ib.adnxs.com/it?an_audit=0&e=wqT_3QLhCWzhBAAAAwDWAAUBCJSC6foFEMGjnrXYm-PkXhj_EQEUASo2CZqZAQEIqT8REQkEGQAFAQjgPyEREgApEQkAMQUauADgPzD7-toHOO5OQO5OSAJQkd-wTVj4mXNgAGjI34wBeI_dBIABAYoBA1VTRJIBAQbwVZgBAaABAagBAbABALgBAsABA8gBAtABANgBAOABAPABAIoCPHVmKCdhJywgMzM5MzUyMCwgMTU5OTc1MDQyMCk7dWYoJ3InLCAxNjIyNzkzMTMsIDE1GR_0DgGSAvkDIWdGazdGZ2lGellBVEVKSGZzRTBZQUNENG1YTXdBRGdBUUFSSTdrNVEtX3JhQjFnQVlQX19fXzhQYUFCd0FYZ0JnQUVCaUFFQmtBRUJtQUVCb0FFQnFBRURzQUVBdVFGMXF3MXNtcG1wUDhFQmRhc05iSnFacVRfSkFYd2xrdlZtdVBBXzJRRUFBQUFBQUFEd1AtQUJBUFVCQUFBQUFKZ0NBS0FDQUxVQ0FBQUFBTDBDQUFBQUFNQUNBY2dDQWRBQ0FkZ0NBZUFDQU9nQ0FQZ0NBSUFEQVpnREFhZ0RoYzJBRTdvRENVNVpUVEk2TkRBMU51QURwaVNJQkFDUUJBQ1lCQUhCQkFBQUFBCYMIeVFRCQkBARhOZ0VBUEVFAQsJASBDSUJkZ2ZxUVUJDxhBRHdQN0VGDQ0UQUFBREJCHT8AeRUoDEFBQU4yKAAAWi4oAPBANEFXSUpfQUY0djN4QV9nRjhJX1BBWUlHQTFWVFJJZ0dBSkFHQVpnR0FLRUdtcG1abVptWnFULW9CZ0d5QmlRSkEBYAkBAFIJBwUBAFoFBgkBAGgJBwEBQEM0QmdvLpoCiQEhMUJLWkhBNv0BMC1KbHpJQVFvQURHYW0Fa1htcFB6b0pUbGxOTWpvME1EVTJRS1lrUxHpDFBBX1URDAxBQUFXHQwAWR0MAGEdDABjHQzwpGVBQS7YAgDgAsqoTYADAYgDAJADAJgDFKADAaoDAMAD4KgByAMA2AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAqAQAsgQMCAAQABgAIAAwADgAuAQAwAQAyAQA0gQPMTAwOTQjTllNMjo0MDU22gQCCAHgBADwBJHfsE2CBRpvcmcucHJlYmlkLm1vYmlsZS5hcGkxZGVtb4gFAZgFAKAF_3X8sKoFJDZhZjFlZmQ3LWJlNmMtNDlmMS04MTk2LWViNjI0NWI5ZWFjZsAFAMkFAGX5FPA_0gUJCQULOAAAANgFAeAFAfAFAfoFBAGwKJAGAZgGALgGAMEGAR8wAADwP9AG1jPaBhYKEAkRGQFcEAAYAOAGDPIGAggAgAcBiAcAoAdBugcOAUgEGAAJ-CRAAMgHj90E0gcNFXMwEAAYANoHBggAEAAYAA..&s=46d93f84d8459a2ca773485e5721255200b9f0ed&pp=${AUCTION_PRICE}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        if (usePrebid && !TextUtils.isEmpty(cacheId)) {
            request = new PublisherAdRequest.Builder()
                    .addCustomTargeting("hb_cache_id", cacheId)
                    .addCustomTargeting("hb_pb", "0.50")
                    .build();
        } else {
            request = new PublisherAdRequest.Builder().build();
        }

        adView.loadAd(request);
    }

    private void loadMoPubNativeNative(final boolean usePrebid) {
        removePreviousAds();
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
        String cacheId = CacheManager.save("{\n" +
                "  \"ver\": \"1.2\",\n" +
                "  \"assets\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"img\": {\n" +
                "        \"type\": 3,\n" +
                "        \"url\": \"https://vcdn.adnxs.com/p/creative-image/7e/71/90/27/7e719027-80ef-4664-9b6d-a763da4cea4e.png\",\n" +
                "        \"w\": 300,\n" +
                "        \"h\": 250,\n" +
                "        \"ext\": {\n" +
                "          \"appnexus\": {\n" +
                "            \"prevent_crop\": 0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"data\": {\n" +
                "        \"type\": 1,\n" +
                "        \"value\": \"AppNexus\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"title\": {\n" +
                "        \"text\": \"This is an RTB ad\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"link\": {\n" +
                "    \"url\": \"https://nym1-ib.adnxs.com/click?mpmZmZmZqT-amZmZmZmpPwAAAAAAAOA_mpmZmZmZqT-amZmZmZmpP8GRp4bdjMle__________8UQVpfAAAAAHu99gBuJwAAbicAAAIAAACRL6wJ-MwcAAAAAABVU0QAVVNEAAEAAQDILwAAAAABAgMCAAAAAMYAfyvwMQAAAAA./bcr=AAAAAAAA8D8=/pp=${AUCTION_PRICE}/cnd=%211BKZHAiFzYATEJHfsE0Y-JlzIAQoADGamZmZmZmpPzoJTllNMjo0MDU2QKYkSQAAAAAAAPA_UQAAAAAAAAAAWQAAAAAAAAAAYQAAAAAAAAAAaQAAAAAAAAAAcQAAAAAAAAAAeAA./cca=MTAwOTQjTllNMjo0MDU2/bn=77455/clickenc=http%3A%2F%2Fappnexus.com\"\n" +
                "  },\n" +
                "  \"eventtrackers\": [\n" +
                "    {\n" +
                "      \"event\": 1,\n" +
                "      \"method\": 1,\n" +
                "      \"url\": \"https://nym1-ib.adnxs.com/it?an_audit=0&e=wqT_3QLhCWzhBAAAAwDWAAUBCJSC6foFEMGjnrXYm-PkXhj_EQEUASo2CZqZAQEIqT8REQkEGQAFAQjgPyEREgApEQkAMQUauADgPzD7-toHOO5OQO5OSAJQkd-wTVj4mXNgAGjI34wBeI_dBIABAYoBA1VTRJIBAQbwVZgBAaABAagBAbABALgBAsABA8gBAtABANgBAOABAPABAIoCPHVmKCdhJywgMzM5MzUyMCwgMTU5OTc1MDQyMCk7dWYoJ3InLCAxNjIyNzkzMTMsIDE1GR_0DgGSAvkDIWdGazdGZ2lGellBVEVKSGZzRTBZQUNENG1YTXdBRGdBUUFSSTdrNVEtX3JhQjFnQVlQX19fXzhQYUFCd0FYZ0JnQUVCaUFFQmtBRUJtQUVCb0FFQnFBRURzQUVBdVFGMXF3MXNtcG1wUDhFQmRhc05iSnFacVRfSkFYd2xrdlZtdVBBXzJRRUFBQUFBQUFEd1AtQUJBUFVCQUFBQUFKZ0NBS0FDQUxVQ0FBQUFBTDBDQUFBQUFNQUNBY2dDQWRBQ0FkZ0NBZUFDQU9nQ0FQZ0NBSUFEQVpnREFhZ0RoYzJBRTdvRENVNVpUVEk2TkRBMU51QURwaVNJQkFDUUJBQ1lCQUhCQkFBQUFBCYMIeVFRCQkBARhOZ0VBUEVFAQsJASBDSUJkZ2ZxUVUJDxhBRHdQN0VGDQ0UQUFBREJCHT8AeRUoDEFBQU4yKAAAWi4oAPBANEFXSUpfQUY0djN4QV9nRjhJX1BBWUlHQTFWVFJJZ0dBSkFHQVpnR0FLRUdtcG1abVptWnFULW9CZ0d5QmlRSkEBYAkBAFIJBwUBAFoFBgkBAGgJBwEBQEM0QmdvLpoCiQEhMUJLWkhBNv0BMC1KbHpJQVFvQURHYW0Fa1htcFB6b0pUbGxOTWpvME1EVTJRS1lrUxHpDFBBX1URDAxBQUFXHQwAWR0MAGEdDABjHQzwpGVBQS7YAgDgAsqoTYADAYgDAJADAJgDFKADAaoDAMAD4KgByAMA2AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAqAQAsgQMCAAQABgAIAAwADgAuAQAwAQAyAQA0gQPMTAwOTQjTllNMjo0MDU22gQCCAHgBADwBJHfsE2CBRpvcmcucHJlYmlkLm1vYmlsZS5hcGkxZGVtb4gFAZgFAKAF_3X8sKoFJDZhZjFlZmQ3LWJlNmMtNDlmMS04MTk2LWViNjI0NWI5ZWFjZsAFAMkFAGX5FPA_0gUJCQULOAAAANgFAeAFAfAFAfoFBAGwKJAGAZgGALgGAMEGAR8wAADwP9AG1jPaBhYKEAkRGQFcEAAYAOAGDPIGAggAgAcBiAcAoAdBugcOAUgEGAAJ-CRAAMgHj90E0gcNFXMwEAAYANoHBggAEAAYAA..&s=46d93f84d8459a2ca773485e5721255200b9f0ed&pp=${AUCTION_PRICE}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        if (usePrebid && !TextUtils.isEmpty(cacheId)) {
            RequestParameters mRP = new RequestParameters.Builder().keywords("hb_pb:0.50,hb_cache_id:" + cacheId).build();
            Log.d("Prebid", mRP.getKeywords());
            mMoPubNative.makeRequest(mRP);
        } else {
            mMoPubNative.makeRequest();
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
//        cta.setText(ad.getCallToAction());
//        cta.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getClickUrl()));
//                startActivity(browserIntent);
//            }
//        });
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

    public void loadDFPWithoutPrebid(View view) {
        PrebidServerAdapter.testingNativeNative = false;
        loadDFPCustomRendering(false);
    }

    public void loadDFPWithPrebid(View view) {
        PrebidServerAdapter.testingNativeNative = false;
        loadDFPCustomRendering(true);
    }

    public void loadMoPubWithoutPrebid(View view) {
        PrebidServerAdapter.testingNativeNative = false;
        loadMoPubNativeNative(false);
    }

    public void loadMoPubWithPrebid(View view) {
        PrebidServerAdapter.testingNativeNative = false;
        loadMoPubNativeNative(true);
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

//        final PublisherAdRequest publisherAdView = new PublisherAdRequest(this);
//        publisherAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                LogUtil.d("ad loaded");
//            }
//        });
//        publisherAdView.setAdUnitId(Constants.DFP_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS);
//        publisherAdView.setAdSizes(AdSize.FLUID);
//        adFrame.addView(publisherAdView);
        final PublisherAdRequest publisherAdRequest = new PublisherAdRequest.Builder()
                    .addCustomTargeting("hb_pb", "0.80")
//                    .addCustomTargeting("hb_cache_id", cacheId)
                .build();
        nativeAdUnit.fetchDemand(publisherAdRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                if (resultCode == ResultCode.SUCCESS) {
                    loadDfp(publisherAdRequest);
                }
                Toast.makeText(PrebidNativeActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDfp(PublisherAdRequest publisherAdRequest) {
        adLoader = new AdLoader.Builder(this, "/19968336/Wei_test_native_native")
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
                .forCustomTemplateAd("11885766", new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {

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
            nativeAdUnit.fetchDemand(mMoPubNative, new OnCompleteListener() {
                @Override
                public void onComplete(ResultCode resultCode) {
                    if (resultCode == ResultCode.SUCCESS) {
                        String keywords = "hb_pb:0.50";
                        if (nativeAdUnit.getCacheId() != null)
                            keywords += ",hb_cache_id:" + nativeAdUnit.getCacheId();
                        RequestParameters mRP = new RequestParameters.Builder().keywords(keywords).build();
                        Log.d("Prebid", mRP.getKeywords());
                        mMoPubNative.makeRequest(mRP);
                    }
                    Toast.makeText(PrebidNativeActivity.this, "Native Ad Unit: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
