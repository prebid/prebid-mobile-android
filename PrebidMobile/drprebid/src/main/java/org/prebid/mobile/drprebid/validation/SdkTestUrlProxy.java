package org.prebid.mobile.drprebid.validation;

import android.content.Context;

import com.google.android.gms.ads.MobileAds;
import com.mopub.network.MoPubRequestQueue;
import com.mopub.network.Networking;
import com.mopub.volley.Request;
import com.mopub.volley.RequestQueue;

import okhttp3.OkHttpClient;

public class SdkTestUrlProxy {

    public SdkTestUrlProxy(Context context) {
        MoPubRequestQueue requestQueue = Networking.getRequestQueue(context);
        requestQueue.addRequestFinishedListener(mRequestFinishedListener);

        requestQueue.removeRequestFinishedListener(mRequestFinishedListener);
    }

    private final RequestQueue.RequestFinishedListener mRequestFinishedListener = request -> {
        
    };
}
