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

import androidx.annotation.NonNull;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.errors.VastParseError;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.OmEventTracker;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.video.vast.Tracking;
import org.prebid.mobile.rendering.video.vast.*;

import java.util.ArrayList;

import static org.prebid.mobile.rendering.parser.AdResponseParserVast.*;

public class CreativeModelsMakerVast extends CreativeModelsMaker {

    private static final String TAG = CreativeModelsMakerVast.class.getSimpleName();
    public static final String HTML_CREATIVE_TAG = "HTML";

    static final String VIDEO_CREATIVE_TAG = "Video";

    @NonNull private final AdLoadListener listener;

    private AdUnitConfiguration adConfiguration;

    private AdResponseParserVast rootVastParser;
    private AdResponseParserVast latestVastWrapperParser;

    private String adLoaderIdentifier;
    private String viewableUrl;

    public CreativeModelsMakerVast(
            String adLoaderIdentifier,
            @NonNull AdLoadListener listener
    ) {
        this.listener = listener;
        this.adLoaderIdentifier = adLoaderIdentifier;
    }

    @Override
    public void makeModels(AdUnitConfiguration adConfiguration, AdResponseParserBase... parsers) {
        if (adConfiguration == null) {
            notifyErrorListener("Successful ad response but has a null config to continue ");
            return;
        }

        this.adConfiguration = adConfiguration;

        if (parsers == null) {
            notifyErrorListener("Parsers results are null.");
            return;
        }

        if (parsers.length != 2) {
            notifyErrorListener("2 VAST result parsers are required");
            return;
        }

        rootVastParser = (AdResponseParserVast) parsers[0];
        latestVastWrapperParser = (AdResponseParserVast) parsers[1];

        if (rootVastParser == null || latestVastWrapperParser == null) {
            notifyErrorListener("One of parsers is null.");
            return;
        }

        makeModelsContinued();
    }

    public void setViewableUrl(String url) {
        this.viewableUrl = url;
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
            rootVastParser.getAllTrackings(rootVastParser, 0);
            rootVastParser.getImpressions(rootVastParser, 0);
            rootVastParser.getClickTrackings(rootVastParser, 0);
            final String videoErrorUrl = rootVastParser.getError(rootVastParser, 0);
            final String vastClickThroughUrl = rootVastParser.getClickThroughUrl(rootVastParser, 0);
            final String videoDuration = latestVastWrapperParser.getVideoDuration(latestVastWrapperParser, 0);
            final String skipOffset = latestVastWrapperParser.getSkipOffset(latestVastWrapperParser, 0);
            final AdVerifications adVerifications = rootVastParser.getAdVerification(latestVastWrapperParser, 0);

            checkVideoDuration(Utils.getMsFrom(videoDuration));

            Result result = new Result();
            result.loaderIdentifier = adLoaderIdentifier;

            TrackingManager trackingManager = TrackingManager.getInstance();
            OmEventTracker omEventTracker = new OmEventTracker();

            VideoCreativeModel videoModel = new VideoCreativeModel(trackingManager, omEventTracker, adConfiguration);

            videoModel.setName(VIDEO_CREATIVE_TAG);

            videoModel.setMediaUrl(latestVastWrapperParser.getMediaFileUrl(latestVastWrapperParser, 0));
            videoModel.setMediaDuration(Utils.getMsFrom(videoDuration));
            videoModel.setSkipOffset(Utils.getMsFrom(skipOffset));
            videoModel.setAdVerifications(adVerifications);
            videoModel.setAuid(rootVastParser.getVast().getAds().get(0).getId());
            videoModel.setWidth(latestVastWrapperParser.getWidth());
            videoModel.setHeight(latestVastWrapperParser.getHeight());
            videoModel.setViewableUrl(viewableUrl);
            //put tracking urls into element.
            for (VideoAdEvent.Event videoEvent : VideoAdEvent.Event.values()) {
                videoModel.getVideoEventUrls().put(videoEvent, rootVastParser.getTrackingByType(videoEvent));
            }

            //put impression urls into element
            ArrayList<String> impUrls = new ArrayList<>();
            for (Impression impression : rootVastParser.getImpressions()) {
                impUrls.add(impression.getValue());
            }
            videoModel.getVideoEventUrls().put(VideoAdEvent.Event.AD_IMPRESSION, impUrls);

            //put click urls into element
            ArrayList<String> clickTrackingUrls = new ArrayList<>();
            for (ClickTracking clickTracking : rootVastParser.getClickTrackings()) {
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

            CreativeModel endCardModel = new CreativeModel(trackingManager, omEventTracker, adConfiguration);
            endCardModel.setName(HTML_CREATIVE_TAG);
            endCardModel.setHasEndCard(true);

            // Create CompanionAd object
            Companion companionAd = AdResponseParserVast.getCompanionAd(latestVastWrapperParser.getVast()
                                                                                               .getAds()
                                                                                               .get(0)
                                                                                               .getInline());
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


                AdUnitConfiguration endCardConfig = new AdUnitConfiguration();
                endCardConfig.setRewardManager(adConfiguration.getRewardManager());
                endCardConfig.setAdFormat(AdFormat.INTERSTITIAL);
                endCardConfig.setRewarded(adConfiguration.isRewarded());
                endCardConfig.getRewardManager().setRewardedExt(adConfiguration.getRewardManager().getRewardedExt());
                endCardConfig.setHasEndCard(true);
                endCardModel.setAdConfiguration(endCardConfig);


                endCardModel.setRequireImpressionUrl(false);
                result.creativeModels.add(endCardModel);

                // Flag that video creative has a corresponding end card
                videoModel.setHasEndCard(true);
                adConfiguration.setHasEndCard(true);
            }
            adConfiguration.setInterstitialSize(videoModel.getWidth() + "x" + videoModel.getHeight());
            listener.onCreativeModelReady(result);
        } catch (Exception e) {
            LogUtil.error(TAG, "Video failed with: " + e.getMessage());
            notifyErrorListener("Video failed: " + e.getMessage());
        }
    }

    private void notifyErrorListener(String msg) {
        listener.onFailedToLoadAd(new AdException(AdException.INTERNAL_ERROR, msg), adLoaderIdentifier);
    }

    private void checkVideoDuration(long currentDuration) throws VastParseError {
        if (adConfiguration != null && adConfiguration.getMaxVideoDuration() != null) {
            long maxDuration = adConfiguration.getMaxVideoDuration() * 1000;
            if (currentDuration > maxDuration) {
                throw new VastParseError("Video duration can't be more then ad unit max video duration: " + maxDuration + " (current duration: " + currentDuration + ")");
            }
        }
    }

}
