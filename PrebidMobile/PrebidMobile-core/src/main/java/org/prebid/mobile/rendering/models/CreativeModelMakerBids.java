/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.models;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.loading.VastParserExtractor;
import org.prebid.mobile.rendering.models.internal.VastExtractorResult;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.video.OmEventTracker;

import java.util.ArrayList;

public class CreativeModelMakerBids {

    private static final String TAG = CreativeModelMakerBids.class.getSimpleName();

    @Nullable
    private String viewableUrl;

    @NonNull private final AdLoadListener listener;
    private final VastParserExtractor parserExtractor = new VastParserExtractor(this::handleExtractorResult);

    private AdUnitConfiguration adConfiguration;

    public CreativeModelMakerBids(
            @NonNull AdLoadListener listener
    ) {
        this.listener = listener;
    }

    public void makeModels(
            AdUnitConfiguration adConfiguration,
            BidResponse bidResponse
    ) {
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

        Context context = PrebidContextHolder.getContext();
        JSLibraryManager jsScriptsManager = JSLibraryManager.getInstance(context);
        if (!jsScriptsManager.checkIfScriptsDownloadedAndStartDownloadingIfNot()) {
            notifyErrorListener("JS libraries has not been downloaded yet. Starting downloading...");
            return;
        }

        if (adConfiguration.isRewarded()) {
            adConfiguration.getRewardManager().setRewardedExt(winningBid.getRewardedExt());
        }

        viewableUrl = winningBid.getBurl();

        if (bidResponse.isVideo()) {
            makeVideoModels(adConfiguration, winningBid.getAdm());
        } else {
            parseAcj(adConfiguration, bidResponse);
        }
    }

    public void makeVideoModels(AdUnitConfiguration adConfiguration, String vast) {
        this.adConfiguration = adConfiguration;
        this.adConfiguration.setAdFormat(AdFormat.VAST);
        parserExtractor.extract(vast);
    }

    public void cancel() {
        if (parserExtractor != null) {
            parserExtractor.cancel();
        }
    }

    private void notifyErrorListener(String msg) {
        listener.onFailedToLoadAd(new AdException(AdException.INTERNAL_ERROR, msg), null);
    }

    private void parseAcj(AdUnitConfiguration adConfiguration, BidResponse bidResponse) {
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
        model.setViewableUrl(viewableUrl);

        adConfiguration.setInterstitialSize(model.getWidth(), model.getHeight());
        result.creativeModels.add(model);
        result.transactionState = "bid";

        listener.onCreativeModelReady(result);
    }

    private String getAdHtml(AdUnitConfiguration adConfiguration, Bid bid) {
        String html = "";

        if (bid == null) {
            LogUtil.error(TAG, "getAdHtml: Failed. Bid is null. Returning empty string.");
            return html;
        }

        html = bid.getAdm();

        return html;
    }

    private void handleExtractorResult(VastExtractorResult result) {
        final String loadIdentifier = result.getLoadIdentifier();

        if (result.hasException()) {
            listener.onFailedToLoadAd(result.getAdException(), loadIdentifier);
            return;
        }

        CreativeModelsMakerVast vastModelMaker = new CreativeModelsMakerVast(loadIdentifier, listener);
        vastModelMaker.setViewableUrl(viewableUrl);
        vastModelMaker.makeModels(adConfiguration, result.getVastResponseParserArray());
    }
}
