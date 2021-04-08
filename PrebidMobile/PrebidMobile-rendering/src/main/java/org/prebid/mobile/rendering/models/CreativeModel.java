package org.prebid.mobile.rendering.models;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
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
    private AdConfiguration mAdConfiguration;
    //helper to get the right creative class
    private String mName;
    private int mDisplayDurationInSeconds = 0;
    //all - creative width
    private int mWidth = 0;

    //all - creative height
    private int mHeight = 0;

    //all - creative html
    private String mHtml;

    @Nullable
    private Integer mRefreshMax;

    HashMap<TrackingEvent.Events, ArrayList<String>> mTrackingURLs = new HashMap<>();

    protected TrackingManager mTrackingManager;
    protected OmEventTracker mOmEventTracker;

    //all - a unique transaction state of the ad
    private String mTransactionState;

    //all - resolved ri url of an ad
    private String mImpressionUrl;

    // Determines whether an impression is needed
    // For end cards, an impression is not necessary
    private boolean mRequireImpressionUrl = true;

    //all - resolved rc url of an ad
    private String mClickUrl;

    private String mTracking;
    private String mTargetUrl;

    // Flags that the creative is part of a transaction with an end card
    // This is important for the display layer to perform end card functions
    private boolean mHasEndCard = false;

    public CreativeModel(TrackingManager trackingManager, OmEventTracker omEventTracker, AdConfiguration adConfiguration) {
        mTrackingManager = trackingManager;
        mAdConfiguration = adConfiguration;
        mOmEventTracker = omEventTracker;
    }

    //tracking firing here from Model always
    public void registerTrackingEvent(TrackingEvent.Events event, ArrayList<String> urls) {
        mTrackingURLs.put(event, urls);
    }

    //Tracking an event
    public void trackDisplayAdEvent(TrackingEvent.Events event) {
        handleOmTracking(event);
        trackEventNamed(event);
    }

    public void trackEventNamed(TrackingEvent.Events event) {
        ArrayList<String> trackingUrls = mTrackingURLs.get(event);

        if (trackingUrls == null || trackingUrls.isEmpty()) {
            OXLog.debug(TAG, "Event" + event + ": url not found for tracking");
            return;
        }

        //Impression tracker changes
        if (event.equals(TrackingEvent.Events.IMPRESSION)) {
            mTrackingManager.fireEventTrackingImpressionURLs(trackingUrls);
        }
        else {
            //for everything else, use standard logic with no redirection check
            //clicks(rc), would go through GetOriginalUrlTask path, for any redirection related task
            //TODO: Check if we can merge redirection check into our standard BaseNetwork class,
            //for all requests(adrequest & recordEvents)
            mTrackingManager.fireEventTrackingURLs(trackingUrls);
        }
    }

    public void registerActiveOmAdSession(OmAdSessionManager omAdSessionManager) {
        mOmEventTracker.registerActiveAdSession(omAdSessionManager);
    }

    private void handleOmTracking(TrackingEvent.Events event) {
        //checking if this click is made on the end card so that we could track it in the scope
        //of OM video session
        if (mHasEndCard && event == TrackingEvent.Events.CLICK) {
            mOmEventTracker.trackOmVideoAdEvent(VideoAdEvent.Event.AD_CLICK);
        }
        else {
            mOmEventTracker.trackOmHtmlAdEvent(event);
        }
    }

    public AdConfiguration getAdConfiguration() {
        return mAdConfiguration;
    }

    public void setAdConfiguration(AdConfiguration adConfiguration) {
        mAdConfiguration = adConfiguration;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getDisplayDurationInSeconds() {
        return mDisplayDurationInSeconds;
    }

    public void setDisplayDurationInSeconds(int displayDurationInSeconds) {
        mDisplayDurationInSeconds = displayDurationInSeconds;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public String getHtml() {
        return mHtml;
    }

    public void setHtml(String html) {
        mHtml = html;
    }

    @Nullable
    public Integer getRefreshMax() {
        return mRefreshMax;
    }

    public void setRefreshMax(
        @Nullable
            Integer refreshMax) {
        mRefreshMax = refreshMax;
    }

    public String getTransactionState() {
        return mTransactionState;
    }

    public void setTransactionState(String transactionState) {
        mTransactionState = transactionState;
    }

    public String getImpressionUrl() {
        return mImpressionUrl;
    }

    public void setImpressionUrl(String impressionUrl) {
        mImpressionUrl = impressionUrl;
    }

    public boolean isRequireImpressionUrl() {
        return mRequireImpressionUrl;
    }

    public void setRequireImpressionUrl(boolean requireImpressionUrl) {
        mRequireImpressionUrl = requireImpressionUrl;
    }

    public String getClickUrl() {
        return mClickUrl;
    }

    public void setClickUrl(String clickUrl) {
        mClickUrl = clickUrl;
    }

    public String getTracking() {
        return mTracking;
    }

    public void setTracking(String tracking) {
        mTracking = tracking;
    }

    public boolean hasEndCard() {
        return mHasEndCard;
    }

    public void setHasEndCard(boolean hasEndCard) {
        mHasEndCard = hasEndCard;
    }

    public void setTargetUrl(String targetUrl) {
        mTargetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return mTargetUrl;
    }
}
