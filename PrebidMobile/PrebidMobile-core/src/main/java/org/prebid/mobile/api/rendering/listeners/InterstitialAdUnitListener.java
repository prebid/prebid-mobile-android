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

package org.prebid.mobile.api.rendering.listeners;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialAdUnit;

/**
 * Listener interface representing InterstitialAdUnit events.
 * All methods will be invoked on the main thread.
 */
public interface InterstitialAdUnitListener {
    /**
     * Executed when the ad is loaded and is ready for display.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdLoaded(InterstitialAdUnit interstitialAdUnit);

    /**
     * Executed when the ad is displayed on screen.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdDisplayed(InterstitialAdUnit interstitialAdUnit);

    /**
     * Executed when an error is encountered on initialization / loading or display step.
     *
     * @param interstitialAdUnit view of the corresponding event.
     * @param exception  exception containing detailed message and error type.
     */
    void onAdFailed(InterstitialAdUnit interstitialAdUnit, AdException exception);

    /**
     * Executed when interstitialAdUnit is clicked.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdClicked(InterstitialAdUnit interstitialAdUnit);

    /**
     * Executed when interstitialAdUnit is closed.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdClosed(InterstitialAdUnit interstitialAdUnit);
}
