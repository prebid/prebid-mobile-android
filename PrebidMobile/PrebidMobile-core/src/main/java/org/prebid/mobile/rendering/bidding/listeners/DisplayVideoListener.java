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

package org.prebid.mobile.rendering.bidding.listeners;

/**
 * Listener interface representing Display video events.
 * All methods will be invoked on the main thread.
 */
public interface DisplayVideoListener {
    // Called when the video complete its playback
    void  onVideoCompleted();

    // Called when the video playback is not visible
    void  onVideoPaused();

    // Called when the video is paused and visibility constraints are satisfied again
    void  onVideoResumed();

    // Called when the video playback is unmuted
    void  onVideoUnMuted();

    // Called when the video playback is muted
    void  onVideoMuted();
}
