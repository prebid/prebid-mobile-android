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

import org.prebid.mobile.api.exceptions.AdException;

public interface DisplayViewListener {
    // Called every time an ad had loaded and is ready for display
    void onAdLoaded();

    // Called every time the ad is displayed on the screen
    void onAdDisplayed();

    // Called whenever the load process fails to produce a viable ad
    void onAdFailed(AdException exception);

    // Called when the banner view will launch a dialog on top of the current view
    void onAdClicked();

    // Called when the banner view has dismissed the modal on top of the current view
    void onAdClosed();
}
