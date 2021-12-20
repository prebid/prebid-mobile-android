package org.prebid.mobile.admob;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;

import java.util.UUID;

public class PrebidAdMobRequest extends AdRequest {

    public static final String EXTRA_RESPONSE_ID = "prebid_response_id";

    private Bundle extras;

    public static PrebidAdMobRequest create() {
        AdRequest.Builder requestBuilder = new AdRequest.Builder();
        return create(requestBuilder);
    }

    public static PrebidAdMobRequest create(Builder requestBuilder) {
        Bundle extras = new Bundle();
        requestBuilder.addCustomEventExtrasBundle(PrebidBannerAdapter.class, extras);
        PrebidAdMobRequest request = new PrebidAdMobRequest(requestBuilder);
        request.extras = extras;
        return request;
    }

    private PrebidAdMobRequest(@NonNull Builder builder) {
        super(builder);
    }

    public void setResponseId(String responseId) {
        extras.putString(EXTRA_RESPONSE_ID, responseId);
    }

}
