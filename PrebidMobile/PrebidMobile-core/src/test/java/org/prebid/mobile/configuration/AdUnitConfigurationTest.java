package org.prebid.mobile.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;
import java.util.UUID;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdUnitConfigurationTest {

    private AdUnitConfiguration subject;

    @Before
    public void setUp() throws Exception {
        subject = new AdUnitConfiguration();
    }

    @Test
    public void createSubject_checkInitState() {
        EnumSet<AdFormat> adFormats = subject.getAdFormats();
        assertNotNull(adFormats);
        assertEquals(0, adFormats.size());
    }

    @Test
    public void addAdFormat_elementsAddedWithoutDuplicates() {
        subject.addAdFormat(AdFormat.BANNER);

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.BANNER), subject.getAdFormats());

        subject.addAdFormat(AdFormat.INTERSTITIAL);

        assertEquals(2, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.INTERSTITIAL), subject.getAdFormats());

        subject.addAdFormat(AdFormat.INTERSTITIAL);

        assertEquals(2, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.INTERSTITIAL), subject.getAdFormats());

        subject.addAdFormat(null);

        assertEquals(2, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.INTERSTITIAL), subject.getAdFormats());
    }

    @Test
    public void addNativeAdFormat_initNativeConfiguration() {
        assertNull(subject.getNativeConfiguration());

        subject.addAdFormat(AdFormat.NATIVE);

        assertNotNull(subject.getNativeConfiguration());
    }

    @Test
    public void setAdFormat_clearAllAndAddOnlyOne() {
        subject.addAdFormat(AdFormat.BANNER);
        subject.addAdFormat(AdFormat.INTERSTITIAL);

        assertEquals(2, subject.getAdFormats().size());

        subject.setAdFormat(AdFormat.VAST);

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.VAST), subject.getAdFormats());

        subject.setAdFormat(null);

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.VAST), subject.getAdFormats());
    }

    @Test
    public void setNativeAdFormat_initNativeConfiguration() {
        assertNull(subject.getNativeConfiguration());

        subject.setAdFormat(AdFormat.NATIVE);

        assertNotNull(subject.getNativeConfiguration());
    }

    @Test
    public void setAdFormats_addCorrespondingElements() {
        subject.setAdUnitFormats(null);

        assertEquals(0, subject.getAdFormats().size());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.DISPLAY));

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), subject.getAdFormats());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.VIDEO));

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.VAST), subject.getAdFormats());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO));

        assertEquals(2, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), subject.getAdFormats());
    }

    @Test
    public void setAdFormatsNewApi_addCorrespondingElements() {
        subject.setAdUnitFormats(null);

        assertEquals(0, subject.getAdFormats().size());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER));

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), subject.getAdFormats());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.VIDEO));

        assertEquals(1, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.VAST), subject.getAdFormats());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO));

        assertEquals(2, subject.getAdFormats().size());
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), subject.getAdFormats());
    }

    @Test
    public void fingerprintIsAValidRandomBasedUUID() {
        // Assert
        String uuidString = subject.getFingerprint();
        assertNotNull(uuidString);
        assertNotNull(UUID.fromString(uuidString)); // valid UUID
        assertEquals(4, UUID.fromString(uuidString).version()); // version 4 (random-based)
    }

}