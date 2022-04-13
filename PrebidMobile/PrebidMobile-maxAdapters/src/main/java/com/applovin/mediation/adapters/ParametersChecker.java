package com.applovin.mediation.adapters;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;

import java.util.HashMap;
import java.util.Map;

import static com.applovin.mediation.adapters.PrebidMAXMediationAdapter.EXTRA_RESPONSE_ID;

class ParametersChecker {

    @Nullable
    public static String getResponseId(
            MaxAdapterResponseParameters parameters,
            OnError onErrorListener
    ) {
        if (parameters == null || parameters.getCustomParameters() == null || parameters.getLocalExtraParameters() == null) {
            onErrorListener.onError(1001, "Parameters are empty!");
            return null;
        }

        Bundle serverParameters = parameters.getCustomParameters();
        Map<String, Object> extras = parameters.getLocalExtraParameters();
        if (!extras.containsKey(EXTRA_RESPONSE_ID) || !(extras.get(EXTRA_RESPONSE_ID) instanceof String)) {
            onErrorListener.onError(1002, "Response id is null");
            return null;
        }

        String responseId = (String) extras.get(EXTRA_RESPONSE_ID);
        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (!ParametersMatcher.doParametersMatch(serverParameters, prebidParameters)) {
            onErrorListener.onError(1003, "Parameters don't match");
            return null;
        }

        return responseId;
    }

    @Nullable
    public static BidResponse getBidResponse(
            String responseId,
            OnError onErrorListener
    ) {
        if (responseId == null) {
            return null;
        }

        BidResponse response = BidResponseCache.getInstance().popBidResponse(responseId);
        if (response == null) {
            onErrorListener.onError(1004, "There's no response for id: " + responseId);
            return null;
        }

        return response;
    }

    public interface OnError {

        void onError(
                int code,
                String error
        );

    }

}
