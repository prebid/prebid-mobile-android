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

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.video.OmEventTracker;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.video.vast.AdVerifications;
import org.prebid.mobile.rendering.video.vast.ClickTracking;
import org.prebid.mobile.rendering.video.vast.Companion;
import org.prebid.mobile.rendering.video.vast.Impression;
import org.prebid.mobile.rendering.video.vast.Tracking;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import static org.prebid.mobile.rendering.parser.AdResponseParserVast.RESOURCE_FORMAT_HTML;
import static org.prebid.mobile.rendering.parser.AdResponseParserVast.RESOURCE_FORMAT_IFRAME;
import static org.prebid.mobile.rendering.parser.AdResponseParserVast.RESOURCE_FORMAT_STATIC;

public class CreativeModelsMakerVast extends CreativeModelsMaker {

    private static final String TAG = CreativeModelsMakerVast.class.getSimpleName();
    public static final String HTML_CREATIVE_TAG = "HTML";

    static final String VIDEO_CREATIVE_TAG = "Video";

    @NonNull
    private final AdLoadListener mListener;

    private AdConfiguration mAdConfiguration;

    private AdResponseParserVast mRootVastParser;
    private AdResponseParserVast mLatestVastWrapperParser;

    private String mAdLoaderIdentifier;

    public CreativeModelsMakerVast(String adLoaderIdentifier,
                                   @NonNull
                                       AdLoadListener listener) {
        mListener = listener;
        mAdLoaderIdentifier = adLoaderIdentifier;
    }

    @Override
    public void makeModels(AdConfiguration adConfiguration, AdResponseParserBase... parsers) {
        if (adConfiguration == null) {
            notifyErrorListener("Successful ad response but has a null config to continue ");
            return;
        }

        mAdConfiguration = adConfiguration;

        if (parsers == null) {
            notifyErrorListener("Parsers results are null.");
            return;
        }

        if (parsers.length != 2) {
            notifyErrorListener("2 VAST result parsers are required");
            return;
        }

        mRootVastParser = (AdResponseParserVast) parsers[0];
        mLatestVastWrapperParser = (AdResponseParserVast) parsers[1];

        if (mRootVastParser == null || mLatestVastWrapperParser == null) {
            notifyErrorListener("One of parsers is null.");
            return;
        }

        makeModelsContinued();
    }

    private void makeModelsContinued() {
        try {
            // TODO: If we want to support a VAST Buffet, we'll need to put the following in a
            // TODO: loop and make a model for each Ad object in the Buffet
            // TODO: Until then, we'll only make one model

            /***
             * We pre parse the impressions and trackings for faster reading at
             * video time. DO NOT REMOVE THESE LINES
             */
            mRootVastParser.getAllTrackings(mRootVastParser, 0);
            mRootVastParser.getImpressions(mRootVastParser, 0);
            mRootVastParser.getClickTrackings(mRootVastParser, 0);
            final String videoErrorUrl = mRootVastParser.getError(mRootVastParser, 0);
            final String vastClickThroughUrl = mRootVastParser.getClickThroughUrl(mRootVastParser, 0);
            final String videoDuration = mLatestVastWrapperParser.getVideoDuration(mLatestVastWrapperParser, 0);
            final String skipOffset = mLatestVastWrapperParser.getSkipOffset(mLatestVastWrapperParser, 0);
            final AdVerifications adVerifications = mRootVastParser.getAdVerification(mLatestVastWrapperParser, 0);

            Result result = new Result();
            result.loaderIdentifier = mAdLoaderIdentifier;

            TrackingManager trackingManager = TrackingManager.getInstance();
            OmEventTracker omEventTracker = new OmEventTracker();

            VideoCreativeModel videoModel = new VideoCreativeModel(trackingManager, omEventTracker, mAdConfiguration);

            videoModel.setName(VIDEO_CREATIVE_TAG);

            videoModel.setMediaUrl(mLatestVastWrapperParser.getMediaFileUrl(mLatestVastWrapperParser, 0));
            videoModel.setMediaDuration(Utils.getMsFrom(videoDuration));
            videoModel.setSkipOffset(Utils.getMsFrom(skipOffset));
            videoModel.setAdVerifications(adVerifications);
            videoModel.setAuid(mRootVastParser.getVast().getAds().get(0).getId());
            videoModel.setWidth(mLatestVastWrapperParser.getWidth());
            videoModel.setHeight(mLatestVastWrapperParser.getHeight());
            //put tracking urls into element.
            for (VideoAdEvent.Event videoEvent : VideoAdEvent.Event.values()) {
                videoModel.getVideoEventUrls().put(videoEvent, mRootVastParser.getTrackingByType(videoEvent));
            }

            //put impression urls into element
            ArrayList<String> impUrls = new ArrayList<>();
            for (Impression impression : mRootVastParser.getImpressions()) {
                impUrls.add(impression.getValue());
            }
            videoModel.getVideoEventUrls().put(VideoAdEvent.Event.AD_IMPRESSION, impUrls);

            //put click urls into element
            ArrayList<String> clickTrackingUrls = new ArrayList<>();
            for (ClickTracking clickTracking : mRootVastParser.getClickTrackings()) {
                clickTrackingUrls.add(clickTracking.getValue());
            }
            videoModel.getVideoEventUrls().put(VideoAdEvent.Event.AD_CLICK, clickTrackingUrls);

            //put error vastURL into element
            ArrayList<String> errorUrls = new ArrayList<>();
            errorUrls.add(videoErrorUrl);
            videoModel.getVideoEventUrls().put(VideoAdEvent.Event.AD_ERROR, errorUrls);

            //put click through url into element
            videoModel.setVastClickthroughUrl(vastClickThroughUrl);

            result.creativeModels = new ArrayList<>();
            result.creativeModels.add(videoModel);

            CreativeModel endCardModel = new CreativeModel(trackingManager, omEventTracker, mAdConfiguration);
            endCardModel.setName(HTML_CREATIVE_TAG);
            endCardModel.setHasEndCard(true);

            // Create CompanionAd object
            Companion companionAd = AdResponseParserVast.getCompanionAd(mLatestVastWrapperParser.getVast().getAds().get(0).getInline());
            if (companionAd != null) {
                switch (AdResponseParserVast.getCompanionResourceFormat(companionAd)) {
                    case RESOURCE_FORMAT_HTML:
                        endCardModel.setHtml(companionAd.getHtmlResource().getValue());
                        break;
                    case RESOURCE_FORMAT_IFRAME:
                        endCardModel.setHtml(companionAd.getIFrameResource().getValue());
                        break;
                    case RESOURCE_FORMAT_STATIC:
                        endCardModel.setHtml(String.format("<div id=\"ad\" align=\"center\">\n"
                                                           + "<a href=\"%s\">\n"
                                                           + "<img src=\"%s\"></a>\n"
                                                           + "</div>",
                                                           companionAd.getCompanionClickThrough().getValue(),
                                                           companionAd.getStaticResource().getValue()));
                        break;
                }

                if (companionAd.getCompanionClickThrough() != null) {
                    endCardModel.setClickUrl(companionAd.getCompanionClickThrough().getValue());
                }

                if (companionAd.getCompanionClickTracking() != null) {
                    clickTrackingUrls = new ArrayList<>();
                    clickTrackingUrls.add(companionAd.getCompanionClickTracking().getValue());
                    endCardModel.registerTrackingEvent(TrackingEvent.Events.CLICK, clickTrackingUrls);
                }

                Tracking creativeViewTracking = AdResponseParserVast.findTracking(companionAd.getTrackingEvents());
                if (creativeViewTracking != null && Utils.isNotBlank(creativeViewTracking.getValue())) {
                    ArrayList<String> creativeViewTrackingUrls = new ArrayList<>();
                    creativeViewTrackingUrls.add(creativeViewTracking.getValue());
                    endCardModel.registerTrackingEvent(TrackingEvent.Events.IMPRESSION, creativeViewTrackingUrls);
                }

                endCardModel.setWidth(Integer.parseInt(companionAd.getWidth()));
                endCardModel.setHeight(Integer.parseInt(companionAd.getHeight()));
                endCardModel.setAdConfiguration(new AdConfiguration());
                endCardModel.getAdConfiguration().setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
                endCardModel.setRequireImpressionUrl(false);
                result.creativeModels.add(endCardModel);

                // Flag that video creative has a corresponding end card
                videoModel.setHasEndCard(true);
            }
            mAdConfiguration.setInterstitialSize(videoModel.getWidth() + "x" + videoModel.getHeight());
            mListener.onCreativeModelReady(result);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Video failed with: " + e.getMessage());
            notifyErrorListener("Video failed: " + e.getMessage());
        }
    }

    private void notifyErrorListener(String msg) {
        mListener.onFailedToLoadAd(new AdException(AdException.INTERNAL_ERROR, msg), mAdLoaderIdentifier);
    }
}
