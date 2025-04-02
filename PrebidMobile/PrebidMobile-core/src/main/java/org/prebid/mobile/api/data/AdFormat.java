package org.prebid.mobile.api.data;

import java.util.EnumSet;

/**
 * Internal ad format. Must be set up only inside the SDK.
 */
public enum AdFormat {
    BANNER,
    INTERSTITIAL,
    NATIVE,
    VAST;

    public static EnumSet<AdFormat> fromSet(
            EnumSet<AdUnitFormat> adUnitFormats,
            boolean isInterstitial
    ) {
        if (adUnitFormats == null || adUnitFormats.isEmpty())
            throw new NullPointerException("List of ad unit formats must contain at least one item.");

        EnumSet<AdFormat> result = EnumSet.noneOf(AdFormat.class);
        for (AdUnitFormat format : adUnitFormats) {
            if (format == AdUnitFormat.BANNER) {
                if (isInterstitial) {
                    result.add(AdFormat.INTERSTITIAL);
                } else {
                    result.add(AdFormat.BANNER);
                }
            }
            if (format == AdUnitFormat.VIDEO) {
                result.add(AdFormat.VAST);
            }
        }
        return result;
    }
}