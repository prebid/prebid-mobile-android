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

package org.prebid.mobile.rendering.bidding.interfaces;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.rendering.models.AdDetails;

public interface InterstitialViewListener {
    /**
     * This is triggered whenever the AD is rendered on the screen.
     */
    void onAdLoaded(InterstitialView interstitialView, AdDetails adDetails);

    /**
     * When AdModel fails to load for whatever reason
     *
     * @param error The AdException received when trying to load the Ad
     */
    void onAdFailed(InterstitialView interstitialView, AdException error);

    /**
     * When a loaded ad is displayed
     */
    void onAdDisplayed(InterstitialView interstitialView);

    /**
     * When an ad has finished refreshing.
     */
    void onAdCompleted(InterstitialView interstitialView);

    /**
     * When an ad was clicked
     */
    void onAdClicked(InterstitialView interstitialView);

    /**
     * When an expanded banner ad was closed
     */
    void onAdClickThroughClosed(InterstitialView interstitialView);

    void onAdClosed(InterstitialView interstitialView);
}
