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

import android.util.Log;

import org.prebid.mobile.rendering.models.TrackingEvent;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;

import java.lang.ref.WeakReference;

public class OmEventTracker {
    private static final String TAG = OmEventTracker.class.getSimpleName();

    private WeakReference<OmAdSessionManager> mWeakReferenceOmAdSessionManager;

    public void registerActiveAdSession(OmAdSessionManager omAdSessionManager) {
        mWeakReferenceOmAdSessionManager = new WeakReference<>(omAdSessionManager);
    }

    public void trackOmVideoAdEvent(VideoAdEvent.Event event) {
        if (mWeakReferenceOmAdSessionManager == null || mWeakReferenceOmAdSessionManager.get() == null) {
            Log.w(TAG, "Unable to trackOmVideoAdEvent: AdSessionManager is null");
            return;
        }

        OmAdSessionManager omAdSessionManager = mWeakReferenceOmAdSessionManager.get();
        omAdSessionManager.trackAdVideoEvent(event);
    }

    public void trackOmHtmlAdEvent(TrackingEvent.Events event) {
        if (mWeakReferenceOmAdSessionManager == null || mWeakReferenceOmAdSessionManager.get() == null) {
            Log.w(TAG, "Unable to trackOmHtmlAdEvent: AdSessionManager is null");
            return;
        }
        OmAdSessionManager omAdSessionManager = mWeakReferenceOmAdSessionManager.get();
        omAdSessionManager.trackDisplayAdEvent(event);
    }

    public void trackOmPlayerStateChange(InternalPlayerState playerState) {
        if (mWeakReferenceOmAdSessionManager == null || mWeakReferenceOmAdSessionManager.get() == null) {
            Log.w(TAG, "Unable to trackOmPlayerStateChange: AdSessionManager is null");
            return;
        }

        OmAdSessionManager omAdSessionManager = mWeakReferenceOmAdSessionManager.get();
        omAdSessionManager.trackPlayerStateChangeEvent(playerState);
    }

    public void trackVideoAdStarted(float duration, float volume) {
        if (mWeakReferenceOmAdSessionManager == null || mWeakReferenceOmAdSessionManager.get() == null) {
            Log.w(TAG, "Unable to trackVideoAdStarted: AdSessionManager is null");
            return;
        }

        OmAdSessionManager omAdSessionManager = mWeakReferenceOmAdSessionManager.get();
        omAdSessionManager.videoAdStarted(duration, volume);
    }

    public void trackNonSkippableStandaloneVideoLoaded(boolean isAutoPlay) {
        if (mWeakReferenceOmAdSessionManager == null || mWeakReferenceOmAdSessionManager.get() == null) {
            Log.w(TAG, "Unable to trackVideoAdStarted: AdSessionManager is null");
            return;
        }

        OmAdSessionManager omAdSessionManager = mWeakReferenceOmAdSessionManager.get();
        omAdSessionManager.nonSkippableStandaloneVideoAdLoaded(isAutoPlay);
    }
}
