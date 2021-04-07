package com.openx.apollo.video;

import com.openx.apollo.models.TrackingEvent;

public class VideoAdEvent extends TrackingEvent{

    public enum Event {

        /*
         * WARNING, IMPORTANT: The first group of events corresponds one
         * to one with an index in the AdResponseParserVast class for VAST
         * event tracking.  Any miscellaneous ones can be appended on the
         * list below.
         */
        AD_CREATIVEVIEW,
        AD_START,
        AD_FIRSTQUARTILE,
        AD_MIDPOINT,
        AD_THIRDQUARTILE,
        AD_COMPLETE,
        AD_MUTE,
        AD_UNMUTE,
        AD_PAUSE,
        AD_REWIND,
        AD_RESUME,
        AD_FULLSCREEN,
        AD_EXITFULLSCREEN,
        AD_EXPAND,
        AD_COLLAPSE,
        AD_ACCEPTINVITATION,
        AD_ACCEPTINVITATIONLINEAR,
        AD_CLOSELINEAR,
        AD_CLOSE,
        AD_SKIP,
        AD_ERROR,
        AD_IMPRESSION,
        AD_CLICK

    }
}
