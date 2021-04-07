package com.openx.apollo.networking.parameters;

import android.app.Activity;

import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.openrtb.BidRequest;
import com.openx.apollo.models.openrtb.bidRequests.Device;
import com.openx.apollo.networking.targeting.Targeting;
import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.utils.helpers.AdIdManager;
import com.openx.apollo.utils.helpers.AppInfoManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static com.apollo.test.utils.ResourceUtils.assertJsonEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, qualifiers = "w1920dp-h1080dp")
public class DeviceInfoParameterBuilderTest {

    private final int SCREEN_WIDTH = 1920;
    private final int SCREEN_HEIGHT = 1080;

    @Before
    public void setUp() throws Exception {
        ManagersResolver.getInstance().prepare(Robolectric.buildActivity(Activity.class).create().get());
    }

    @Test
    public void testAppendBuilderParameters() throws Exception {
        BidRequest expectedBidRequest = new BidRequest();
        final Device expectedBidRequestDevice = expectedBidRequest.getDevice();
        final String ipAddress = "192.168.0.1";
        final String carrier = "carrier";

        Targeting.setDeviceIpAddress(ipAddress);
        Targeting.setCarrier(carrier);

        AdConfiguration adConfiguration = new AdConfiguration();

        ParameterBuilder builder = new DeviceInfoParameterBuilder(adConfiguration);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        expectedBidRequestDevice.os = DeviceInfoParameterBuilder.PLATFORM_VALUE;
        expectedBidRequestDevice.w = SCREEN_WIDTH;
        expectedBidRequestDevice.h = SCREEN_HEIGHT;
        expectedBidRequestDevice.language = Locale.getDefault().getLanguage();
        expectedBidRequestDevice.ip = ipAddress;
        expectedBidRequestDevice.carrier = carrier;
        expectedBidRequestDevice.osv = "4.4";
        expectedBidRequestDevice.os = "Android";
        expectedBidRequestDevice.model = "robolectric";
        expectedBidRequestDevice.make = "unknown";
        expectedBidRequestDevice.pxratio = 1f;
        expectedBidRequestDevice.ua = AppInfoManager.getUserAgent();
        expectedBidRequestDevice.ifa = AdIdManager.getAdId();
        expectedBidRequestDevice.lmt = AdIdManager.isLimitAdTrackingEnabled() ? 1 : 0;

        assertJsonEquals(expectedBidRequest.getJsonObject(),
                     adRequestInput.getBidRequest().getJsonObject());
    }
}