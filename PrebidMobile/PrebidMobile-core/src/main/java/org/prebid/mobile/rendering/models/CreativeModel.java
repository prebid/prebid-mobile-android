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

import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.video.OmEventTracker;
import org.prebid.mobile.rendering.video.VideoAdEvent;

import java.util.ArrayList;
import java.util.HashMap;

// CreativeModel is visible to the publisher, and defines:
// --- displayDurationInSeconds indicates the time the creative will display for
// -------- A negative value indicates that this field has not been set
// -------- A value of 0 indicates an indefinite time
// -------- A positive value indicates that this creative will be displayed for that many seconds
// --- width is the width of the creative, in pixels
// --- height is the height of the creative, in pixels
// --- trackDisplayAdEvent functions take an enum or string, and cause the tracking URLs associated with those events to be fired
// --- registerTrackingEvent takes a key and list of urls, and adds those urls, associated with the key, as a tracking event to the model

public class CreativeModel {

    private static String TAG = CreativeModel.class.getSimpleName();

    //internal data
    private AdUnitConfiguration adConfiguration;
    //helper to get the right creative class
    private String name;
    private int displayDurationInSeconds = 0;
    //all - creative width
    private int width = 0;

    //all - creative height
    private int height = 0;

    //all - creative html
    private String html;

    @Nullable private Integer refreshMax;

    HashMap<TrackingEvent.Events, ArrayList<String>> trackingURLs = new HashMap<>();

    protected TrackingManager trackingManager;
    protected OmEventTracker omEventTracker;

    //all - a unique transaction state of the ad
    private String transactionState;

    //all - resolved ri url of an ad
    private String impressionUrl;
    private String viewableUrl;

    // Determines whether an impression is needed
    // For end cards, an impression is not necessary
    private boolean requireImpressionUrl = true;

    //all - resolved rc url of an ad
    private String clickUrl;

    private String tracking;
    private String targetUrl;

    // Flags that the creative is part of a transaction with an end card
    // This is important for the display layer to perform end card functions
    private boolean hasEndCard = false;

    public CreativeModel(
            TrackingManager trackingManager,
            OmEventTracker omEventTracker,
            AdUnitConfiguration adConfiguration
    ) {
        this.trackingManager = trackingManager;
        this.adConfiguration = adConfiguration;
        this.omEventTracker = omEventTracker;

        if (adConfiguration != null) {
            setImpressionUrl(adConfiguration.getImpressionUrl());
        }
    }

    //tracking firing here from Model always
    public void registerTrackingEvent(TrackingEvent.Events event, ArrayList<String> urls) {
        trackingURLs.put(event, urls);
    }

    //Tracking an event
    public void trackDisplayAdEvent(TrackingEvent.Events event) {
        handleOmTracking(event);
        trackEventNamed(event);
    }

    public void trackEventNamed(TrackingEvent.Events event) {
        ArrayList<String> trackingUrls = trackingURLs.get(event);

        if (trackingUrls == null || trackingUrls.isEmpty()) {
            LogUtil.debug(TAG, "Event" + event + ": url not found for tracking");
            return;
        }

        //Impression tracker changes
        if (event.equals(TrackingEvent.Events.IMPRESSION)) {
            trackingManager.fireEventTrackingImpressionURLs(trackingUrls);
        }
        else {
            //for everything else, use standard logic with no redirection check
            //clicks(rc), would go through GetOriginalUrlTask path, for any redirection related task
            //TODO: Check if we can merge redirection check into our standard BaseNetwork class,
            //for all requests(adrequest & recordEvents)
            trackingManager.fireEventTrackingURLs(trackingUrls);
        }
    }

    public void registerActiveOmAdSession(OmAdSessionManager omAdSessionManager) {
        omEventTracker.registerActiveAdSession(omAdSessionManager);
    }

    private void handleOmTracking(TrackingEvent.Events event) {
        //checking if this click is made on the end card so that we could track it in the scope
        //of OM video session
        if (hasEndCard && event == TrackingEvent.Events.CLICK) {
            omEventTracker.trackOmVideoAdEvent(VideoAdEvent.Event.AD_CLICK);
        } else {
            omEventTracker.trackOmHtmlAdEvent(event);
        }
    }

    public AdUnitConfiguration getAdConfiguration() {
        return adConfiguration;
    }

    public void setAdConfiguration(AdUnitConfiguration adConfiguration) {
        this.adConfiguration = adConfiguration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayDurationInSeconds() {
        return displayDurationInSeconds;
    }

    public void setDisplayDurationInSeconds(int displayDurationInSeconds) {
        this.displayDurationInSeconds = displayDurationInSeconds;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    @Nullable
    public Integer getRefreshMax() {
        return refreshMax;
    }

    public void setRefreshMax(
        @Nullable
            Integer refreshMax) {
        this.refreshMax = refreshMax;
    }

    public String getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(String transactionState) {
        this.transactionState = transactionState;
    }

    public String getImpressionUrl() {
        return impressionUrl;
    }

    public void setImpressionUrl(String impressionUrl) {
        this.impressionUrl = impressionUrl;
    }

    public String getViewableUrl() {
        return viewableUrl;
    }

    public void setViewableUrl(String viewableUrl) {
        this.viewableUrl = viewableUrl;
    }

    public boolean isRequireImpressionUrl() {
        return requireImpressionUrl;
    }

    public void setRequireImpressionUrl(boolean requireImpressionUrl) {
        this.requireImpressionUrl = requireImpressionUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    public String getTracking() {
        return tracking;
    }

    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    public boolean hasEndCard() {
        return hasEndCard;
    }

    public void setHasEndCard(boolean hasEndCard) {
        this.hasEndCard = hasEndCard;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }
}
