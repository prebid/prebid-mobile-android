package org.prebid.mobile.api.data;

import androidx.annotation.Nullable;

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

    /**
     * Maps internal ad formats back to the public {@link AdUnitFormat} values.
     * {@link #BANNER} and {@link #INTERSTITIAL} both map to {@link AdUnitFormat#BANNER},
     * {@link #VAST} maps to {@link AdUnitFormat#VIDEO}. {@link #NATIVE} has no public
     * counterpart and is skipped.
     */
    public static EnumSet<AdUnitFormat> toSet(@Nullable EnumSet<AdFormat> adFormats) {
        EnumSet<AdUnitFormat> result = EnumSet.noneOf(AdUnitFormat.class);
        if (adFormats == null) return result;

        for (AdFormat format : adFormats) {
            if (format == AdFormat.BANNER || format == AdFormat.INTERSTITIAL) {
                result.add(AdUnitFormat.BANNER);
            }
            if (format == AdFormat.VAST) {
                result.add(AdUnitFormat.VIDEO);
            }
        }
        return result;
    }
}