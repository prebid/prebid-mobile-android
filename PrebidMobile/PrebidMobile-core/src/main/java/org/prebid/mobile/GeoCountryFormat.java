/*
 *    Copyright 2018-2025 Prebid.org, Inc.
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

package org.prebid.mobile;

/**
 * Format used for the auto-detected {@code device.geo.country} in the bid
 * request. See {@link PrebidMobile#setGeoCountryFormat(GeoCountryFormat)}.
 */
public enum GeoCountryFormat {

    /**
     * ISO-3166-1 alpha-2 (e.g. "US") — the format the OS telephony/geocoder
     * APIs return. Current default to preserve existing behavior.
     */
    ALPHA2,

    /**
     * ISO-3166-1 alpha-3 (e.g. "USA") — what the OpenRTB spec requires for
     * {@code device.geo.country}. Opt in via
     * {@link PrebidMobile#setGeoCountryFormat(GeoCountryFormat)}. Planned to
     * become the default in Prebid SDK 4.0.
     */
    ALPHA3
}
