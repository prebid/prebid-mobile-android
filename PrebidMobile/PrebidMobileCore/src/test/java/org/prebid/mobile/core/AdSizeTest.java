package org.prebid.mobile.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.core.AdSize;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class AdSizeTest {
    @Test
    public void testSizeCreation() {
        AdSize size = new AdSize(320, 50);
        assertEquals(320, size.getWidth());
        assertEquals(50, size.getHeight());
    }
}
