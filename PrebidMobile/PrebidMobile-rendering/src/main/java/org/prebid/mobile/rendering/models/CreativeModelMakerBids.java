package org.prebid.mobile.rendering.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.loading.VastParserExtractor;
import org.prebid.mobile.rendering.models.internal.VastExtractorResult;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.helpers.MacrosResolutionHelper;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.video.OmEventTracker;

import java.util.ArrayList;

public class CreativeModelMakerBids {
    private static final String TAG = CreativeModelMakerBids.class.getSimpleName();

    @NonNull
    private final AdLoadListener mListener;
    private final VastParserExtractor mParserExtractor = new VastParserExtractor(this::handleExtractorResult);

    private AdConfiguration mAdConfiguration;

    public CreativeModelMakerBids(
        @NonNull
            AdLoadListener listener) {
        mListener = listener;
    }

    public void makeModels(AdConfiguration adConfiguration, BidResponse bidResponse) {
        if (adConfiguration == null) {
            notifyErrorListener("Successful ad response but has a null config to continue");
            return;
        }

        if (bidResponse == null || bidResponse.hasParseError()) {
            notifyErrorListener("Bid response is null or has an error.");
            return;
        }

        final Bid winningBid = bidResponse.getWinningBid();
        if (winningBid == null || TextUtils.isEmpty(winningBid.getAdm())) {
            notifyErrorListener("No ad was found.");
            return;
        }

        if (bidResponse.isVideo()) {
            makeVideoModels(adConfiguration, winningBid.getAdm());
        }
        else {
            parseAcj(adConfiguration, bidResponse);
        }
    }

    public void makeVideoModels(AdConfiguration adConfiguration, String vast) {
        mAdConfiguration = adConfiguration;
        mAdConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
        mParserExtractor.extract(vast);
    }

    public void cancel() {
        if (mParserExtractor != null) {
            mParserExtractor.cancel();
        }
    }

    private void notifyErrorListener(String msg) {
        mListener.onFailedToLoadAd(new AdException(AdException.INTERNAL_ERROR, msg), null);
    }

    private void parseAcj(AdConfiguration adConfiguration, BidResponse bidResponse) {
        CreativeModelsMaker.Result result = new CreativeModelsMaker.Result();
        result.creativeModels = new ArrayList<>();

        Bid bid = bidResponse.getWinningBid();
        String adHtml = getAdHtml(adConfiguration, bid);

        CreativeModel model = new CreativeModel(TrackingManager.getInstance(), new OmEventTracker(), adConfiguration);
        model.setName("HTML");
        model.setHtml(adHtml);
        model.setWidth(bid != null ? bid.getWidth() : 0);
        model.setHeight(bid != null ? bid.getHeight() : 0);
        model.setRequireImpressionUrl(false);

        adConfiguration.setInterstitialSize(model.getWidth(), model.getHeight());
        result.creativeModels.add(model);
        result.transactionState = "bid";

        mListener.onCreativeModelReady(result);
    }

    private String getAdHtml(AdConfiguration adConfiguration, Bid bid) {
        String html = "";

        if (bid == null) {
            OXLog.error(TAG, "getAdHtml: Failed. Bid is null. Returning empty string.");
            return html;
        }

        if (!adConfiguration.isNative()) {
            html = bid.getAdm();
        }
        else {
            NativeAdConfiguration nativeAdConfiguration = adConfiguration.getNativeAdConfiguration();
            Prebid prebid = bid.getPrebid();

            html = MacrosResolutionHelper.resolveTargetingMarcos(nativeAdConfiguration.getNativeStylesCreative(),
                                                                 prebid.getTargeting());
        }

        return html;
    }

    private void handleExtractorResult(VastExtractorResult result) {
        final String loadIdentifier = result.getLoadIdentifier();

        if (result.hasException()) {
            mListener.onFailedToLoadAd(result.getAdException(), loadIdentifier);
            return;
        }

        CreativeModelsMaker modelsMaker = new CreativeModelsMakerVast(loadIdentifier, mListener);
        modelsMaker.makeModels(mAdConfiguration, result.getVastResponseParserArray());
    }
}
