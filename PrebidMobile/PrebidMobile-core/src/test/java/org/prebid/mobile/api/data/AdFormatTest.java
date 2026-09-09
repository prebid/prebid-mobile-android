package org.prebid.mobile.api.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.EnumSet;

public class AdFormatTest {

    @Test
    public void adFormatsFromSet_isNotInterstitial_banner() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.BANNER);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.BANNER);

        assertEquals(expected, AdFormat.fromSet(input, false));
    }

    @Test
    public void adFormatsFromSet_isInterstitial_banner() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.BANNER);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.INTERSTITIAL);

        assertEquals(expected, AdFormat.fromSet(input, true));
    }

    @Test
    public void adFormatsFromSet_isNotInterstitial_two() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.VIDEO, AdUnitFormat.BANNER);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.VAST, AdFormat.BANNER);

        assertEquals(expected, AdFormat.fromSet(input, false));
    }

    @Test
    public void adFormatsFromSet_isInterstitial_two() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.VIDEO, AdUnitFormat.BANNER);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.VAST, AdFormat.INTERSTITIAL);

        assertEquals(expected, AdFormat.fromSet(input, true));
    }

    @Test
    public void adFormatsFromSet_video() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.VIDEO);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.VAST);

        assertEquals(expected, AdFormat.fromSet(input, false));
    }

    @Test
    public void adFormatsToSet_banner() {
        assertEquals(EnumSet.of(AdUnitFormat.BANNER), AdFormat.toSet(EnumSet.of(AdFormat.BANNER)));
    }

    @Test
    public void adFormatsToSet_interstitial() {
        assertEquals(EnumSet.of(AdUnitFormat.BANNER), AdFormat.toSet(EnumSet.of(AdFormat.INTERSTITIAL)));
    }

    @Test
    public void adFormatsToSet_vast() {
        assertEquals(EnumSet.of(AdUnitFormat.VIDEO), AdFormat.toSet(EnumSet.of(AdFormat.VAST)));
    }

    @Test
    public void adFormatsToSet_multiformat() {
        EnumSet<AdFormat> input = EnumSet.of(AdFormat.BANNER, AdFormat.VAST);
        EnumSet<AdUnitFormat> expected = EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO);

        assertEquals(expected, AdFormat.toSet(input));
    }

    @Test
    public void adFormatsToSet_nativeHasNoPublicCounterpart() {
        assertEquals(EnumSet.noneOf(AdUnitFormat.class), AdFormat.toSet(EnumSet.of(AdFormat.NATIVE)));
    }

    @Test
    public void adFormatsToSet_null() {
        assertEquals(EnumSet.noneOf(AdUnitFormat.class), AdFormat.toSet(null));
    }

}