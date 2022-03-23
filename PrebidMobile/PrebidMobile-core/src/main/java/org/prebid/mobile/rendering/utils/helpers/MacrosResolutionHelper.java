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

package org.prebid.mobile.rendering.utils.helpers;

import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.models.internal.MacrosModel;

import java.util.Map;

public class MacrosResolutionHelper {
    private static final String TAG = MacrosResolutionHelper.class.getSimpleName();

    private static final String MACROS_TARGETING_MAP_KEY = "%%PATTERN:TARGETINGMAP%%";
    private static final String MACROS_PATTERN_PREFIX = "%%PATTERN:";
    private static final String MACROS_PATTERN_POSTFIX = "%%";

    private static final String REGEX_NON_RESOLVED_MACROS = "\"?" + MACROS_PATTERN_PREFIX + ".*" + MACROS_PATTERN_POSTFIX + "\"?";

    private MacrosResolutionHelper() {

    }

    public static String resolveTargetingMarcos(String creative, Map<String, String> targetingMap) {
        if (targetingMap == null) {
            LogUtil.error(TAG, "resolveMacros: Failed. Targeting map is null.");
            return creative;
        }

        String targetingMapJson = new JSONObject(targetingMap).toString();
        creative = replace(MACROS_TARGETING_MAP_KEY, creative, targetingMapJson);

        for (Map.Entry<String, String> entry : targetingMap.entrySet()) {
            String macros = MACROS_PATTERN_PREFIX + entry.getKey() + MACROS_PATTERN_POSTFIX;
            creative = replace(macros, creative, entry.getValue());
        }

        creative = replace(REGEX_NON_RESOLVED_MACROS, creative, "null");

        return creative;
    }

    public static String resolveAuctionMacros(String target, Map<String, MacrosModel> replaceMacrosMap) {
        if (replaceMacrosMap == null || replaceMacrosMap.isEmpty()) {
            LogUtil.error(TAG, "resolveAuctionMacros: Failed. Macros map is null or empty.");
            return target;
        }

        for (Map.Entry<String, MacrosModel> modelEntry : replaceMacrosMap.entrySet()) {
            String macrosKey = modelEntry.getKey();
            String replaceValue = modelEntry.getValue().getReplaceValue();

            target = replace(macrosKey, target, replaceValue);
        }

        return target;
    }

    private static String replace(String macros, String input, String replacement) {
        if (input == null || input.isEmpty()) {
            LogUtil.error(TAG, "replace: Failed. Input string is null or empty.");
            return "";
        }

        if (replacement == null) {
            LogUtil.error(TAG, "replace: Failed. Replacement string is null. Maybe you need to use NativeAdConfiguration.setNativeStylesCreative");
            return "";
        }

        return input.replaceAll(macros, replacement);
    }
}
