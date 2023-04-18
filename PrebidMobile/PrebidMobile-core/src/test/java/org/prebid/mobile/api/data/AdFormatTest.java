package org.prebid.mobile.api.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.EnumSet;

public class AdFormatTest {

    @Test
    public void adFormatsFromSet_isNotInterstitial_banner() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.DISPLAY);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.BANNER);

        assertEquals(expected, AdFormat.fromSet(input, false));
    }

    @Test
    public void adFormatsFromSet_isInterstitial_banner() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.DISPLAY);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.INTERSTITIAL);

        assertEquals(expected, AdFormat.fromSet(input, true));
    }

    @Test
    public void adFormatsFromSet_isNotInterstitial_two() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.VIDEO, AdUnitFormat.DISPLAY);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.VAST, AdFormat.BANNER);

        assertEquals(expected, AdFormat.fromSet(input, false));
    }

    @Test
    public void adFormatsFromSet_isInterstitial_two() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.VIDEO, AdUnitFormat.DISPLAY);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.VAST, AdFormat.INTERSTITIAL);

        assertEquals(expected, AdFormat.fromSet(input, true));
    }

    @Test
    public void adFormatsFromSet_video() {
        EnumSet<AdUnitFormat> input = EnumSet.of(AdUnitFormat.VIDEO);
        EnumSet<AdFormat> expected = EnumSet.of(AdFormat.VAST);

        assertEquals(expected, AdFormat.fromSet(input, false));
    }

}