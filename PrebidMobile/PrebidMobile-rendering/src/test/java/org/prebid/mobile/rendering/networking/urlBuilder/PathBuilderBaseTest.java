package org.prebid.mobile.rendering.networking.urlBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PathBuilderBaseTest {

    @Test
    public void testBuildURLPath() {
        PathBuilderBase pathBuilderBase = new PathBuilderBase();
        String ret_https = pathBuilderBase.buildURLPath("www.domain.com");
        String expected_https = "https://www.domain.com/ma/1.0/";
        assertEquals("Got wrong url: " + ret_https, ret_https, expected_https);
    }
}