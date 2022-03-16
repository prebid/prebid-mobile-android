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

package org.prebid.mobile.rendering.mraid.methods;

import android.view.View;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

public interface InterstitialManagerMraidDelegate {
    boolean collapseMraid();

    void closeThroughJs(WebViewBase viewToClose);

    void displayPrebidWebViewForMraid(final WebViewBase adBaseView,
                                      final boolean isNewlyLoaded,
                                      MraidEvent mraidEvent);

    void displayViewInInterstitial(final View adBaseView,
                                   boolean addOldViewToBackStack,
                                   final MraidEvent expandUrl,
                                   final MraidController.DisplayCompletionListener displayCompletionListener);

    void destroyMraidExpand();
}
