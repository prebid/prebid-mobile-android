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

package org.prebid.mobile.rendering.views;

import android.view.View;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.models.AdDetails;

public abstract class AdViewManagerListener {
    /**
     * A successful load of an ad
     */
    public void adLoaded(AdDetails adDetails) { }

    /**
     * Attach creativeview to AdView
     *
     * @param creative which is ready for display
     */
    public void viewReadyForImmediateDisplay(View creative) { }

    /**
     * Callback for a failure in loading an ad
     *
     * @param error which occurred while loading
     */
    public void failedToLoad(AdException error) { }

    /**
     * When an ad has finished refreshing.
     */
    public void adCompleted() { }

    /**
     * Handle click of a creative
     */
    public void creativeClicked(String url) { }

    /**
     * Handle close of an interstitial ad
     */
    public void creativeInterstitialClosed() { }

    //mraidAdExpanded
    public void creativeExpanded() { }

    //mraidAdCollapsed
    public void creativeCollapsed() { }

    public void creativeMuted() { }

    public void creativeUnMuted() { }

    public void creativePaused() { }

    public void creativeResumed() { }

    public void videoCreativePlaybackFinished() {}
}
