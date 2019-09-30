/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.video.ima;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class PbVideoAdEvent {

    static final String AD_LOAD_SUCCESS_STRING = "Ad loading successful";
    static final String AD_LOAD_FAIL_STRING = "Ad loading failed";

    static final String AD_STARTED_STRING = "Ad playing started";
    static final String AD_DID_REACH_END_STRING = "Ad playing finished";

    static final String AD_CLICKED_STRING = "Ad click-through";

    /**
     *  Type of the event.
     */
    private PbVideoAdEventType type;

    /**
     *  Stringified type of the event.
     */
    private String typeString;

    PbVideoAdEvent(PbVideoAdEventType type, @NonNull String typeString) {
        this.type = type;
        this.typeString = typeString;
    }

    public PbVideoAdEventType getType() {
        return type;
    }

    public String getTypeString() {
        return typeString;
    }
}

final class VideoAdEventFactory {
    private VideoAdEventFactory() {
    }

    static PbVideoAdEvent getAdLoadSuccess(@Nullable String typeString) {
        return new PbVideoAdEvent(PbVideoAdEventType.AD_LOAD_SUCCESS, !TextUtils.isEmpty(typeString) ? typeString : PbVideoAdEvent.AD_LOAD_SUCCESS_STRING);
    }

    static PbVideoAdEvent getAdLoadFail(@Nullable String typeString) {
        return new PbVideoAdEvent(PbVideoAdEventType.AD_LOAD_FAIL, !TextUtils.isEmpty(typeString) ? typeString : PbVideoAdEvent.AD_LOAD_FAIL_STRING);
    }

    static PbVideoAdEvent getAdStarted(@Nullable String typeString) {
        return new PbVideoAdEvent(PbVideoAdEventType.AD_STARTED, !TextUtils.isEmpty(typeString) ? typeString : PbVideoAdEvent.AD_STARTED_STRING);
    }

    static PbVideoAdEvent getAdDidReachEnd(@Nullable String typeString) {
        return new PbVideoAdEvent(PbVideoAdEventType.AD_DID_REACH_END, !TextUtils.isEmpty(typeString) ? typeString : PbVideoAdEvent.AD_DID_REACH_END_STRING);
    }

    static PbVideoAdEvent getAdClicked(@Nullable String typeString) {
        return new PbVideoAdEvent(PbVideoAdEventType.AD_CLICKED, !TextUtils.isEmpty(typeString) ? typeString : PbVideoAdEvent.AD_CLICKED_STRING);
    }

}
