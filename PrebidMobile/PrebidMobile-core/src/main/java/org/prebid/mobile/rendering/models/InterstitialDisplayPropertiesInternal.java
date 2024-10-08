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

import android.content.pm.ActivityInfo;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.configuration.AdUnitConfiguration;

public class InterstitialDisplayPropertiesInternal extends InterstitialDisplayPropertiesPublic {

    public int expandWidth;
    public int expandHeight;
    public int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public int skipDelay = 0;
    public double closeButtonArea = 0;
    public double skipButtonArea = 0;

    public boolean isSoundButtonVisible = false;
    public boolean isMuted = false;
    public boolean isRotationEnabled = false;

    public Position closeButtonPosition = Position.TOP_RIGHT;
    public Position skipButtonPosition = Position.TOP_RIGHT;
    public AdUnitConfiguration config;

    public void resetExpandValues() {
        expandHeight = 0;
        expandWidth = 0;
    }

}
