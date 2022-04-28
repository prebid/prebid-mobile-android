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
import org.prebid.mobile.api.rendering.BannerView;

/**
 * Listener interface representing BannerView events.
 * All methods will be invoked on the main thread.
 */
public interface BannerViewListener {
    /**
     * Executed when the ad is loaded and is ready for display.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdLoaded(BannerView bannerView);

    /**
     * Executed when the ad is displayed on screen.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdDisplayed(BannerView bannerView);

    /**
     * Executed when an error is encountered on initialization / loading or display step.
     *
     * @param bannerView view of the corresponding event.
     * @param exception  exception containing detailed message and error type.
     */
    void onAdFailed(BannerView bannerView, AdException exception);

    /**
     * Executed when bannerView is clicked.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdClicked(BannerView bannerView);

    /**
     * Executed when modal window (e.g. browser) on top of bannerView is closed.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdClosed(BannerView bannerView);
}
