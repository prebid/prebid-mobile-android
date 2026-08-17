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

package org.prebid.mobile.rendering.interstitial;

/**
 * Implemented by an interstitial ad view that can be asked to re-apply cutout/navigation-bar
 * insets to its controls (close, skip, sound, countdown timer, etc.).
 * <p>
 * Lets {@link AdBaseDialog}, which lives in the lower-level {@code rendering.interstitial}
 * package, notify its {@code adViewContainer} without depending on the higher-level
 * {@code api.rendering} package that actually implements this.
 */
public interface ControlInsetsRefresher {

    void refreshControlInsets();

}
