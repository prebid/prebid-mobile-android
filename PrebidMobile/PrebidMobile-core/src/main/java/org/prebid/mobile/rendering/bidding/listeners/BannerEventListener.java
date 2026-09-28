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

import android.view.View;
import androidx.annotation.Nullable;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.api.exceptions.AdException;

public interface BannerEventListener {
    void onPrebidSdkWin();

    void onAdServerWin(View view);

    /**
     * Ad server win that also reports the size of the creative the ad server served, in dp.
     * Event handlers that know the served size should call this instead of
     * {@link #onAdServerWin(View)}, so the banner can report it. Pass null when the size is unknown.
     */
    default void onAdServerWin(View view, @Nullable AdSize adSize) {
        onAdServerWin(view);
    }

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();
}
