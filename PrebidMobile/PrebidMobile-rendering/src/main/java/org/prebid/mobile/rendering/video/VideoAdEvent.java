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

package org.prebid.mobile.rendering.video;

import org.prebid.mobile.rendering.models.TrackingEvent;

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
