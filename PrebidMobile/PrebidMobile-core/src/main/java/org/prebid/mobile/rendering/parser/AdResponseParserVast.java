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

package org.prebid.mobile.rendering.parser;

import android.text.TextUtils;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.errors.VastParseError;
import org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.vast.Ad;
import org.prebid.mobile.rendering.video.vast.AdVerifications;
import org.prebid.mobile.rendering.video.vast.ClickTracking;
import org.prebid.mobile.rendering.video.vast.Companion;
import org.prebid.mobile.rendering.video.vast.Creative;
import org.prebid.mobile.rendering.video.vast.Extension;
import org.prebid.mobile.rendering.video.vast.Impression;
import org.prebid.mobile.rendering.video.vast.InLine;
import org.prebid.mobile.rendering.video.vast.MediaFile;
import org.prebid.mobile.rendering.video.vast.VAST;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdResponseParserVast extends AdResponseParserBase {

    private static final String TAG = AdResponseParserVast.class.getSimpleName();

    public static final int RESOURCE_FORMAT_HTML = 1;
    public static final int RESOURCE_FORMAT_IFRAME = 2;
    public static final int RESOURCE_FORMAT_STATIC = 3;

    private boolean ready;

    private volatile AdResponseParserVast wrappedVASTXml;

    private ArrayList<org.prebid.mobile.rendering.video.vast.Tracking> trackings;
    private ArrayList<ClickTracking> clickTrackings;
    private ArrayList<Impression> impressions;

    private VAST vast;

    public ArrayList<org.prebid.mobile.rendering.video.vast.Tracking> getTrackings() {
        return trackings;
    }

    public ArrayList<Impression> getImpressions() {

        return impressions;
    }

    public ArrayList<ClickTracking> getClickTrackings() {

        return clickTrackings;
    }

    public static class Tracking {

        public final static int EVENT_IMPRESSION = 0;
        public final static int EVENT_CREATIVEVIEW = 1;
        public final static int EVENT_START = 2;
        public final static int EVENT_FIRSTQUARTILE = 3;
        public final static int EVENT_MIDPOINT = 4;
        public final static int EVENT_THIRDQUARTILE = 5;
        public final static int EVENT_COMPLETE = 6;
        public final static int EVENT_MUTE = 7;
        public final static int EVENT_UNMUTE = 8;
        public final static int EVENT_PAUSE = 9;
        public final static int EVENT_REWIND = 10;
        public final static int EVENT_RESUME = 11;
        public final static int EVENT_FULLSCREEN = 12;
        public final static int EVENT_EXITFULLSCREEN = 13;
        public final static int EVENT_EXPAND = 14;
        public final static int EVENT_COLLAPSE = 15;
        public final static int EVENT_ACCEPTINVITATION = 16;//nonlinear
        public final static int EVENT_ACCEPTINVITATIONLINEAR = 17;
        public final static int EVENT_CLOSELINEAR = 18;
        public final static int EVENT_CLOSE = 19;
        public final static int EVENT_SKIP = 20;
        public final static int EVENT_PROGRESS = 21;

        public final static String[] EVENT_MAPPING = new String[]{
            "creativeView",
            "start",
            "firstQuartile",
            "midpoint",
            "thirdQuartile",
            "complete",
            "mute",
            "unmute",
            "pause",
            "rewind",
            "resume",
            "fullscreen",
            "exitFullscreen",
            "expand",
            "collapse",
            "acceptInvitation",
            "acceptInvitationLinear",
            "closeLinear",
            "close",
            "skip",
            "error",
            "impression",
            "click"};

        private int event;

        private String url;

        public Tracking(String event, String url) {
            this.event = findEvent(event);
            this.url = url;
        }

        private int findEvent(String event) {
            for (int i = 0; i < EVENT_MAPPING.length; i++) {
                if (EVENT_MAPPING[i].equals(event)) {
                    return i;
                }
            }
            return -1;
        }

        public int getEvent() {
            return event;
        }

        public String getUrl() {
            return url;
        }
    }

    public AdResponseParserVast(String data) throws VastParseError {
        trackings = new ArrayList<>();
        impressions = new ArrayList<>();
        clickTrackings = new ArrayList<>();
        ready = false;

        try {
            readVAST(data);
        } catch (Exception e) {
            throw new VastParseError(e.getLocalizedMessage());
        }
        ready = true;
    }

    public VAST getVast() {
        return vast;
    }

    private void readVAST(String data) throws XmlPullParserException, IOException {
        String bomFreeString = checkForBOM(data);
        if (bomFreeString != null) {
            data = bomFreeString;
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(data));
        parser.nextTag();

        vast = new VAST(parser);
    }

    @Nullable
    private String checkForBOM(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        String result = null;
        int startIndex = data.indexOf("<");
        if (startIndex > 0) {
            result = data.substring(startIndex);
        }
        return result;
    }

    public List<String> getImpressionTrackerUrl() {
        List<String> urls = new ArrayList<>();
        if (wrappedVASTXml != null && wrappedVASTXml.getImpressionTrackerUrl() != null) {
            urls.addAll(wrappedVASTXml.getImpressionTrackerUrl());
        }

        return urls;
    }

    public String getVastUrl() {
        if (vast.getAds() != null)
            for (Ad ad : vast.getAds()) {

                if (ad.getWrapper() != null && ad.getWrapper().getVastUrl() != null) {

                    return ad.getWrapper().getVastUrl().getValue();
                }
            }
        return null;
    }

    //Returns the best media file fit for the device
    public String getMediaFileUrl(AdResponseParserVast parserVast, int index) {
        String myBestMediaFileURL = null;
        ArrayList<MediaFile> eligibleMediaFiles = new ArrayList<>();
        /**
         * Here we use a recursion pattern to traverse the nested VAST nodes "parserVast",
         * until we reach the last nested node, which should be InLine,
         * and then we get its meduaFile URL. Note that index is hardcoded in the call
         * as 0 for the first Ad node. So we have to figure out a solution for Ad pods.
         */
        if (wrappedVASTXml != null) {
            wrappedVASTXml.getMediaFileUrl(wrappedVASTXml, index);
        }
        /**
         * Now that we have reached the last node, we can get its mediaFileUrl.
         * Note that index is hardcoded in the call
         * as 0 for the first Ad node. So we have to figure out a solution for Ad pods.
         */
        else {

            Ad ad = parserVast.vast.getAds().get(index);

            for (Creative creative : ad.getInline().getCreatives()) {

                if (creative.getLinear() != null) {

                    for (MediaFile mediaFile : creative.getLinear().getMediaFiles()) {
                        if (supportedVideoFormat(mediaFile.getType())) {
                            eligibleMediaFiles.add(mediaFile);
                        }
                    }

                    if (eligibleMediaFiles.size() == 0) {
                        return myBestMediaFileURL;
                    }

                    // choose the one with the highest resolution amongst all
                    MediaFile best = eligibleMediaFiles.get(0);
                    int bestValues = (Utils.isBlank(best.getWidth())
                                      ? 0
                                      : Integer.parseInt(best.getWidth())) * (Utils.isBlank(best.getHeight())
                                                                              ? 0
                                                                              : Integer.parseInt(best.getHeight()));
                    myBestMediaFileURL = best.getValue();

                    for (int i = 0; i < eligibleMediaFiles.size(); i++) {
                        MediaFile current = eligibleMediaFiles.get(i);
                        int currentValues = (Utils.isBlank(current.getWidth())
                                             ? 0
                                             : Integer.parseInt(current.getWidth())) * (Utils.isBlank(current.getHeight())
                                                                                        ? 0
                                                                                        : Integer.parseInt(current.getHeight()));
                        if (currentValues > bestValues) {
                            bestValues = currentValues;
                            best = current;
                            myBestMediaFileURL = best.getValue();
                        }
                    }
                }
            }
        }
        return myBestMediaFileURL;
    }

    @VisibleForTesting
    static boolean supportedVideoFormat(String type) {

        if (!TextUtils.isEmpty(type)) {
            for (int i = 0; i < BasicParameterBuilder.SUPPORTED_VIDEO_MIME_TYPES.length; ++i) {
                if (type.equalsIgnoreCase(BasicParameterBuilder.SUPPORTED_VIDEO_MIME_TYPES[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Impression> getImpressions(AdResponseParserVast parserVast, int index) {
        if (getImpressionEvents(parserVast.vast, index) != null) {
            impressions.addAll(getImpressionEvents(parserVast.vast, index));
        }

        /**
         * Here we use a recursion pattern to traverse the nested VAST nodes "parserVast",
         */
        if (parserVast.wrappedVASTXml != null) {
            getImpressions(parserVast.wrappedVASTXml, index);
        }

        return impressions;
    }

    public ArrayList<org.prebid.mobile.rendering.video.vast.Tracking> getTrackingEvents(VAST vast, int index) {

        Ad ad = vast.getAds().get(index);
        if (ad.getInline() != null) {

            for (Creative creative : ad.getInline().getCreatives()) {

                if (creative.getLinear() != null) {

                    return creative.getLinear().getTrackingEvents();
                }
            }
        }
        else if (ad.getWrapper() != null && ad.getWrapper().getCreatives() != null) {

            for (Creative creative : ad.getWrapper().getCreatives()) {

                if (creative.getLinear() != null) {

                    return creative.getLinear().getTrackingEvents();
                }
                else if (creative.getNonLinearAds() != null) {

                    return creative.getNonLinearAds().getTrackingEvents();
                }
            }
        }
        return null;
    }

    protected ArrayList<Impression> getImpressionEvents(VAST vast, int index) {

        Ad ad = vast.getAds().get(index);
        if (ad.getInline() != null) {

            return ad.getInline().getImpressions();
        }
        else if (ad.getWrapper() != null) {

            return ad.getWrapper().getImpressions();
        }
        return null;
    }

    public ArrayList<org.prebid.mobile.rendering.video.vast.Tracking> getAllTrackings(AdResponseParserVast parserVast, int index) {

        if (getTrackingEvents(parserVast.vast, index) != null) {
            trackings.addAll(getTrackingEvents(parserVast.vast, index));
        }

        /**
         * Here we use a recursion pattern to traverse the nested VAST nodes "parserVast",
         */
        if (parserVast.wrappedVASTXml != null) {
            getAllTrackings(parserVast.wrappedVASTXml, index);
        }

        return trackings;
    }

    public ArrayList<String> getTrackingByType(VideoAdEvent.Event event) {
        Iterator<org.prebid.mobile.rendering.video.vast.Tracking> iterator = trackings.iterator();

        ArrayList<String> urls = new ArrayList<>();

        while (iterator.hasNext()) {
            org.prebid.mobile.rendering.video.vast.Tracking t = iterator.next();
            // Uncomment for debugging only; Else, too many log entries
            // PbLog.debug(TAG, "iterating: " + t.event);
            if (t.getEvent().equals(Tracking.EVENT_MAPPING[event.ordinal()])) {
                // PbLog.debug(TAG, "iterating match: " + t.event);
                urls.add(t.getValue());
            }
        }

        return urls;
    }

    public String getSkipOffset(AdResponseParserVast parserVast, int index) {

        if (wrappedVASTXml != null) {
            return wrappedVASTXml.getSkipOffset(parserVast, index);
        } else {

            Ad ad = parserVast.vast.getAds().get(index);

            if (ad != null && ad.getInline() != null) {

                for (Creative creative : ad.getInline().getCreatives()) {

                    if (creative.getLinear() != null) {

                        return creative.getLinear().getSkipOffset();
                    }
                }
            }
        }
        return null;
    }

    public String getVideoDuration(AdResponseParserVast parserVast, int index) {

        if (wrappedVASTXml != null) {
            return wrappedVASTXml.getVideoDuration(parserVast, index);
        } else {

            Ad ad = parserVast.vast.getAds().get(index);

            if (ad != null && ad.getInline() != null) {

                for (Creative creative : ad.getInline().getCreatives()) {

                    if (creative.getLinear() != null) {

                        return creative.getLinear().getDuration().getValue();
                    }
                }
            }
        }
        return null;
    }

    public AdVerifications getAdVerification(AdResponseParserVast parserVast, int index) {

        Ad ad = parserVast.vast.getAds().get(index);

        if (ad == null || ad.getInline() == null) {
            return null;
        }

        // The Vast Spec declares that there will be either 0 or 1 adVerifications nodes.
        // Return the first one discovered.

        // The Inline object itself can have an adVerifications node
        if (ad.getInline().getAdVerifications() != null) {
            return ad.getInline().getAdVerifications();
        }

        // Walk Extensions and look for adVerifications nodes
        if (ad.getInline().getExtensions() == null) {
            return null;
        }

        ArrayList<Extension> extensions = ad.getInline().getExtensions().getExtensions();
        if (extensions != null) {
            for (Extension extension : extensions) {
                if (extension.getAdVerifications() != null) {
                    return extension.getAdVerifications();
                }
            }
        }

        return null;
    }

    public String getError(AdResponseParserVast parserVast, int index) {

        Ad ad = parserVast.vast.getAds().get(index);

        if (ad != null && ad.getInline() != null && ad.getInline().getError() != null) {
            return ad.getInline().getError().getValue();
        }

        return null;
    }

    public String getClickThroughUrl(AdResponseParserVast parserVast, int index) {

        if (wrappedVASTXml != null) {
            return wrappedVASTXml.getClickThroughUrl(wrappedVASTXml, index);
        } else {

            Ad ad = parserVast.vast.getAds().get(index);

            for (Creative creative : ad.getInline().getCreatives()) {

                if (creative.getLinear() != null && creative.getLinear()
                                                            .getVideoClicks() != null && creative.getLinear()
                                                                                                 .getVideoClicks()
                                                                                                 .getClickThrough() != null) {

                    return creative.getLinear().getVideoClicks().getClickThrough().getValue();
                }
            }
        }
        return null;
    }

    private ArrayList<ClickTracking> findClickTrackings(VAST vast, int index) {

        Ad ad = vast.getAds().get(index);

        if (ad.getInline() != null) {

            for (Creative creative : ad.getInline().getCreatives()) {

                if (creative.getLinear() != null && creative.getLinear().getVideoClicks() != null && creative.getLinear().getVideoClicks().getClickTrackings() != null) {

                    return creative.getLinear().getVideoClicks().getClickTrackings();
                }
            }
        }
        else if (ad.getWrapper() != null && ad.getWrapper().getCreatives() != null) {

            for (Creative creative : ad.getWrapper().getCreatives()) {

                if (creative.getLinear() != null && creative.getLinear().getVideoClicks() != null && creative.getLinear().getVideoClicks().getClickTrackings() != null) {

                    return creative.getLinear().getVideoClicks().getClickTrackings();
                }
            }
        }

        return null;
    }

    public ArrayList<ClickTracking> getClickTrackings(AdResponseParserVast parserVast, int index) {
        ArrayList<ClickTracking> clickTrackingsList = findClickTrackings(parserVast.vast, index);
        if (clickTrackingsList != null) {
            clickTrackings.addAll(clickTrackingsList);
        }

        /**
         * Here we use a recursion pattern to traverse the nested VAST nodes "parserVast",
         */
        if (parserVast.wrappedVASTXml != null) {
            getClickTrackings(parserVast.wrappedVASTXml, index);
        }

        return clickTrackings;
    }

    public List<String> getClickTrackingUrl() {

        List<String> urls = new ArrayList<>();
        if (wrappedVASTXml != null && wrappedVASTXml.getClickTrackingUrl() != null) {
            urls.addAll(wrappedVASTXml.getClickTrackingUrl());
        }

        return urls;
    }

    /**
     * Returns best companion inside InLine
     */
    public static Companion getCompanionAd(@NonNull
                                                   InLine inline) {

        if (inline.getCreatives() == null) {
            return null;
        }

        Companion bestCompanion = null;
        for (Creative creative : inline.getCreatives()) {
            ArrayList<Companion> companionAds = creative.getCompanionAds();

            // Not a companion ad list or empty list
            if (companionAds == null || companionAds.size() == 0) {
                continue;
            }

            for (int i = 0; i < companionAds.size(); i++) {
                try {
                    Companion currentCompanion = companionAds.get(i);
                    if (compareCompanions(currentCompanion, bestCompanion) == 1) {
                        bestCompanion = currentCompanion;
                    }
                }
                catch (IllegalArgumentException e) {
                    LogUtil.error(TAG, e.getMessage());
                }
            }
        }

        return bestCompanion;
    }

    /**
     * Determine which is the better companion
     * If 'companionA' equals 'companionB', return 0
     * If 'companionA' is better, return 1
     * If 'companionB' is better, return 2
     *
     * Ranking rules
     * 1st: HTML > IFRAME > STATIC
     * 2nd: Size
     */
    private static int compareCompanions(Companion companionA, Companion companionB)
    throws IllegalArgumentException {
        if (companionA == null && companionB == null) {
            throw new IllegalArgumentException("No companions to compare") ;
        }
        else if (companionA == null) {
            return 2;
        }
        else if (companionB == null) {
            return 1;
        }

        Integer resourceFormatA = getCompanionResourceFormat(companionA);
        Integer resourceFormatB = getCompanionResourceFormat(companionB);
        if (resourceFormatA == null && resourceFormatB == null) {
            throw new IllegalArgumentException("No companion resources to compare") ;
        }
        else if (resourceFormatA == null) {
            return 2;
        }
        else if (resourceFormatB == null) {
            return 1;
        }
        else if (resourceFormatA < resourceFormatB) {
            return 1;
        }
        else if (resourceFormatA > resourceFormatB) {
            return 2;
        }

        // If resource formats are equal, compare size
        int resolutionA = getResolution(companionA.getWidth(), companionA.getHeight());
        int resolutionB = getResolution(companionB.getWidth(), companionB.getHeight());
        if (resolutionA < resolutionB)  {
            return 2;
        }
        else if (resolutionA > resolutionB) {
            return 1;
        }

        // Companions are equal
        return 0;
    }

    /**
     * Returns product of width and height
     */
    private static int getResolution(String width, String height) {
        int numWidth = Utils.isBlank(width) ? 0 : Integer.parseInt(width);
        int numHeight = Utils.isBlank(height) ? 0 : Integer.parseInt(height);

        return numWidth * numHeight;
    }

    /**
     * Returns companion ad's resource format
     */
    public static Integer getCompanionResourceFormat(Companion companion) {
        if (companion == null) {
            return null;
        }

        if (companion.getHtmlResource() != null) {
            return RESOURCE_FORMAT_HTML;
        }
        else if (companion.getIFrameResource() != null) {
            return RESOURCE_FORMAT_IFRAME;
        }
        else if (companion.getStaticResource() != null) {
            return RESOURCE_FORMAT_STATIC;
        }

        return null;
    }

    /**
     * Searches through ArrayList of Tracking for a specific event
     */
    public static org.prebid.mobile.rendering.video.vast.Tracking findTracking(ArrayList<org.prebid.mobile.rendering.video.vast.Tracking> trackingEvents) {
        if (trackingEvents != null) {
            for (org.prebid.mobile.rendering.video.vast.Tracking tracking : trackingEvents) {
                if (tracking.getEvent().equals("creativeView")) {
                    return tracking;
                }
            }
        }

        return null;
    }

    public synchronized boolean isReady() {

        return ready && (wrappedVASTXml == null || wrappedVASTXml.isReady());
    }


    public void setWrapper(AdResponseParserVast vastXml) {
        wrappedVASTXml = vastXml;
    }

    /**
     * @return null if no wrapped XML is present, a reference to a wrapped VASTXmlParse if it is
     */
    public AdResponseParserVast getWrappedVASTXml() {
        return wrappedVASTXml;
    }

    public int getWidth() {
        try {
            return Integer.parseInt(vast.getAds()
                                        .get(0)
                                        .getInline()
                                        .getCreatives()
                                        .get(0)
                                        .getLinear()
                                        .getMediaFiles()
                                        .get(0)
                                        .getWidth());
        }
        catch (Exception e) {
            return 0;
        }
    }

    public int getHeight() {
        try {
            return Integer.parseInt(vast.getAds()
                                        .get(0)
                                        .getInline()
                                        .getCreatives()
                                        .get(0)
                                        .getLinear()
                                        .getMediaFiles()
                                        .get(0)
                                        .getHeight());
        }
        catch (Exception e) {
            return 0;
        }
    }
}
