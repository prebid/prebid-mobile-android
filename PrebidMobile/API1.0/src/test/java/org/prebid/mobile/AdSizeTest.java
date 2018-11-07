package org.prebid.mobile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdSizeTest {
    @Test
    public void testAdSizeCreation() throws Exception {
        AdSize size = new AdSize(300, 250);
        assertEquals(300, size.getWidth());
        assertEquals(250, size.getHeight());
    }

    @Test
    public void testSizeObjectEquals() throws Exception {
        AdSize size1 = new AdSize(300, 250);
        AdSize size2 = new AdSize(300, 250);
        assertEquals(size1, size2);
        assertEquals(size1.hashCode(), size2.hashCode());
    }
}
