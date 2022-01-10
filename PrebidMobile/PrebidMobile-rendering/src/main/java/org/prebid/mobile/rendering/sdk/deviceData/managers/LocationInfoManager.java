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

package org.prebid.mobile.rendering.sdk.deviceData.managers;

import org.prebid.mobile.rendering.sdk.Manager;
import org.prebid.mobile.rendering.sdk.ManagersResolver;

/**
 * Manager for retrieving location information.
 *
 * @see ManagersResolver
 */
public interface LocationInfoManager extends Manager {
    /**
     * Get the latitude.
     *
     * @return the latitude
     */
    Double getLatitude();

    /**
     * Get the longitude.
     *
     * @return the longitude
     */
    Double getLongitude();

    Float getAccuracy();

    Long getElapsedSeconds();

    boolean isLocationAvailable();

    void resetLocation();
}
