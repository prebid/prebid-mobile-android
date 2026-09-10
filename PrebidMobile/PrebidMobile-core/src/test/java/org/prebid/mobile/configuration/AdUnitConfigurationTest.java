package org.prebid.mobile.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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
    public void setAdUnitFormats_notInterstitial_mapsBannerToBanner() {
        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER), false);

        assertEquals(EnumSet.of(AdFormat.BANNER), subject.getAdFormats());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), false);

        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.VAST), subject.getAdFormats());
    }

    @Test
    public void setAdUnitFormats_emptySet_keepsCurrentValue() {
        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), false);

        subject.setAdUnitFormats(EnumSet.noneOf(AdUnitFormat.class), false);

        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.VAST), subject.getAdFormats());
    }

    @Test
    public void getAdUnitFormats_mapsBackToPublicFormats() {
        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), false);
        assertEquals(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), subject.getAdUnitFormats());

        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), true);
        assertEquals(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), subject.getAdUnitFormats());
    }

    //region ================= Rendering copy
    @Test
    public void copyConstructor_carriesEveryValueOver() {
        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), false);
        subject.setConfigId("config-id");
        subject.setPbAdSlot("ad-slot");
        subject.setAutoRefreshDelay(30);
        subject.setRewarded(true);
        subject.setBuiltInVideo(true);
        subject.setImpOrtbConfig("{\"a\":1}");
        subject.addSize(new AdSize(300, 250));

        AdUnitConfiguration copy = new AdUnitConfiguration(subject);

        assertEquals(subject.getAdFormats(), copy.getAdFormats());
        assertEquals(subject.getConfigId(), copy.getConfigId());
        assertEquals(subject.getPbAdSlot(), copy.getPbAdSlot());
        assertEquals(subject.getAutoRefreshDelay(), copy.getAutoRefreshDelay());
        assertEquals(subject.isRewarded(), copy.isRewarded());
        assertEquals(subject.isBuiltInVideo(), copy.isBuiltInVideo());
        assertEquals(subject.getImpOrtbConfig(), copy.getImpOrtbConfig());
        assertEquals(subject.getSizes(), copy.getSizes());
    }

    @Test
    public void copyConstructor_keepsFingerprintAndBroadcastId() {
        AdUnitConfiguration copy = new AdUnitConfiguration(subject);

        // Plugin event listeners are registered under the fingerprint, and creatives address the
        // event receiver by broadcast id, so both must survive the copy.
        assertEquals(subject.getFingerprint(), copy.getFingerprint());
        assertEquals(subject.getBroadcastId(), copy.getBroadcastId());
    }

    @Test
    public void copyConstructor_sharesTheRewardManager() {
        // The reward is read back through the ad unit after rendering.
        assertSame(subject.getRewardManager(), new AdUnitConfiguration(subject).getRewardManager());
    }

    @Test
    public void copyConstructor_isolatesFormatAndSizeChanges() {
        subject.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO), false);
        AdUnitConfiguration copy = new AdUnitConfiguration(subject);

        copy.setAdFormat(AdFormat.VAST);
        copy.addSize(new AdSize(1, 1));

        assertEquals(EnumSet.of(AdFormat.BANNER, AdFormat.VAST), subject.getAdFormats());
        assertFalse(subject.getSizes().contains(new AdSize(1, 1)));
    }

    @Test
    public void copyConstructor_coversEveryDeclaredField() {
        // Tripwire: a field added to AdUnitConfiguration must also be added to the copy
        // constructor, otherwise rendering silently loses it. Update both together.
        Set<String> expected = new HashSet<>(Arrays.asList(
                "isRewarded", "isBuiltInVideo", "isMuted", "isSoundButtonVisible", "isOriginalAdUnit",
                "hasEndCard", "videoSkipOffset", "autoRefreshDelayInMillis", "skipDelay", "broadcastId",
                "videoInitialVolume", "closeButtonArea", "skipButtonArea", "maxVideoDuration",
                "configId", "pbAdSlot", "interstitialSize", "impressionUrl", "fingerprint", "gpid",
                "impOrtbConfig", "globalOrtbConfig", "closeButtonPosition", "skipButtonPosition",
                "minSizePercentage", "placementType", "adPosition", "appContent", "bannerParameters",
                "videoParameters", "nativeConfiguration", "rewardManager", "adFormats", "adSizes"
        ));

        Set<String> actual = new HashSet<>();
        for (Field field : AdUnitConfiguration.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                actual.add(field.getName());
            }
        }

        assertEquals("AdUnitConfiguration fields changed: update the copy constructor and this list",
                expected, actual);
    }
    //endregion ================= Rendering copy

    @Test
    public void fingerprintIsAValidRandomBasedUUID() {
        // Assert
        String uuidString = subject.getFingerprint();
        assertNotNull(uuidString);
        assertNotNull(UUID.fromString(uuidString)); // valid UUID
        assertEquals(4, UUID.fromString(uuidString).version()); // version 4 (random-based)
    }

}