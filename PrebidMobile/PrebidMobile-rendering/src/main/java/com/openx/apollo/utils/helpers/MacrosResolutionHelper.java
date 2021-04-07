package com.openx.apollo.utils.helpers;

import com.openx.apollo.models.internal.MacrosModel;
import com.openx.apollo.utils.logger.OXLog;

import org.json.JSONObject;

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
            OXLog.error(TAG, "resolveMacros: Failed. Targeting map is null.");
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
            OXLog.error(TAG, "resolveAuctionMacros: Failed. Macros map is null or empty.");
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
            OXLog.error(TAG, "replace: Failed. Input string is null or empty.");
            return "";
        }

        if (replacement == null) {
            OXLog.error(TAG, "replace: Failed. Replacement string is null. Maybe you need to use NativeAdConfiguration.setNativeStylesCreative");
            return "";
        }

        return input.replaceAll(macros, replacement);
    }
}
