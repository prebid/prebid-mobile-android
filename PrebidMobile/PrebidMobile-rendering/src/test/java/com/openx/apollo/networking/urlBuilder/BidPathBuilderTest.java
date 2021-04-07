package com.openx.apollo.networking.urlBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidPathBuilderTest {
    @Test
    public void testBuildUrlPath() {
        assertEquals("https://prebid.openx.net/openrtb2/auction", new BidPathBuilder().buildURLPath(null));
    }
}