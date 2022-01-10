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

package org.prebid.mobile.rendering.models;

import android.graphics.Color;

public class InterstitialDisplayPropertiesPublic {
    // this is the default system dim for dialogs
    private int dimColor = Color.argb(153, 0, 0, 0);

    /**
     * Returns the background opacity set for an interstitial ad
     *
     * @return
     */
    public int getPubBackGroundOpacity() {
        return dimColor;
    }

    /**
     * Sets the background opacity for an interstitial ad
     * Recommended is 1.0f
     *
     * @param dim
     */
    public void setPubBackGroundOpacity(float dim) {
        int alpha;

        float dimAmount;
        if (dim < 0.0f) {
            dimAmount = 0.0f;
        }
        else if (dim > 1.0f) {
            dimAmount = 1.0f;
        }
        else {
            dimAmount = dim;
        }

        alpha = (int) (255 * dimAmount);

        dimColor = Color.argb(alpha, 0, 0, 0);
    }
}
