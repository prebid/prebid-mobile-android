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

    /**
     * The ad server won and its creative is ready to be displayed.
     *
     * @param view   the ad server's creative view.
     * @param adSize size, in dp, of the creative the ad server served, or null when the event
     *               handler does not know it.
     */
    void onAdServerWin(View view, @Nullable AdSize adSize);

    /**
     * @deprecated Use {@link #onAdServerWin(View, AdSize)}, which also reports the served size.
     */
    @Deprecated
    default void onAdServerWin(View view) {
        onAdServerWin(view, null);
    }

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();
}
