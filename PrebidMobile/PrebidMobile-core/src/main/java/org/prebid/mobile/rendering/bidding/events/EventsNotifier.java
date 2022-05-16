package org.prebid.mobile.rendering.bidding.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

/**
 * Class for sending events from bids (seatbid.bid.ext.prebid.events.*)
 */
public class EventsNotifier {

    private static final String TAG = EventsNotifier.class.getSimpleName();

    public static void notify(@Nullable String url) {
        if (url != null) {
            LogUtil.verbose(TAG, "Notify event: " + url);
            ServerConnection.fireAndForget(url);
        }
    }

    /**
     * Parses events in bid.
     *
     * @param bidJson - bid field from seatbid.bid
     */
    @Nullable
    public static String parseEvent(
        @NonNull String eventKey,
        @Nullable JSONObject bidJson
    ) {
        if (bidJson == null) return null;

        JSONObject extJson = bidJson.optJSONObject("ext");
        if (extJson != null) {
            JSONObject prebidJson = extJson.optJSONObject("prebid");
            if (prebidJson != null) {
                JSONObject eventsJson = prebidJson.optJSONObject("events");
                if (eventsJson != null) {
                    String result = eventsJson.optString(eventKey, "");
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        }

        return null;
    }


    @Nullable
    private static String getImpressionEventUrl(@Nullable BidResponse bidResponse) {
        if (bidResponse == null) return null;

        Bid winningBid = bidResponse.getWinningBid();
        if (winningBid != null) {
            Prebid prebid = winningBid.getPrebid();
            return prebid.getImpEventUrl();
        }

        return null;
    }

}
