package org.prebid.mobile;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class BannerAdUnitTest {
    @Test
    public void testBannerAdUnitCreation() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        assertEquals(1, adUnit.getSizes().size());
        assertEquals(AdType.BANNER, FieldUtils.readField(adUnit, "adType", true));
        assertEquals(0, FieldUtils.readField(adUnit, "periodMillis", true));
    }

    @Test
    public void testBannerAdUnitAddSize() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        assertEquals(2, adUnit.getSizes().size());
        adUnit.addAdditionalSize(320, 50);
        assertEquals(2, adUnit.getSizes().size());
    }

    @Test
    public void testAddKeywords() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.setUserKeyword("key", "value");
        adUnit.setUserKeyword("key1", null);
        @SuppressWarnings("unchecked")
        ArrayList<String> keywords = (ArrayList<String>) FieldUtils.readField(adUnit, "keywords", true);
        assertEquals(2, keywords.size());
        assertEquals("key=value", keywords.get(0));
        assertEquals("key1", keywords.get(1));
        adUnit.setUserKeyword("key", "value2");
        assertEquals(3, keywords.size());
        assertEquals("key=value", keywords.get(0));
        assertEquals("key1", keywords.get(1));
        assertEquals("key=value2", keywords.get(2));
        adUnit.removeUserKeyword("key");
        assertEquals(1, keywords.size());
        assertEquals("key1", keywords.get(0));
        adUnit.removeUserKeywords();
        assertEquals(0, keywords.size());
    }
}
