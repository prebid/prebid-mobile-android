package org.prebid.mobile.core;

import android.content.Context;

import java.util.HashMap;

public interface NewDemandAdapter {

    void requestDemand(Context context, RequestParams params, NewDemandAdapterListener listener);

    void stopRequest();

    interface NewDemandAdapterListener {
        void onDemandReady(HashMap<String, String> demand);

        void onDemandFailed(NewResultCode resultCode);

    }
}
