package org.prebid.mobile.rendering.networking.urlBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.enums.Host;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidPathBuilderTest {
    @Test
    public void testBuildUrlPath() {
        final Host custom = Host.CUSTOM;
        custom.setHostUrl("https://prebid.customhost.net/openrtb2/auction");
        PrebidRenderingSettings.setBidServerHost(custom);

        assertEquals("https://prebid.customhost.net/openrtb2/auction", new BidPathBuilder().buildURLPath(null));
    }
}