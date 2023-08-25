package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.BuildConfig;

import static org.junit.Assert.*;

public class MobileSdkPassThroughTest {

    @Test
    public void create_putWrongJsonObject_returnNull() throws JSONException {
        JSONObject jsonObject = new JSONObject("{}");

        MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

        assertNull(subject);
    }

    @Test
    public void create_putObjectWithWrongType_returnNull() throws JSONException {
        JSONObject jsonObject = new JSONObject("{\"prebid\":{\"passthrough\":[{\"type\":\"any\"}]}}");

        MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

        assertNull(subject);
    }

    @Test
    public void create_putObjectWithoutAdConfigurationOrSDKConfig_returnNull() throws JSONException {
        JSONObject jsonObject = new JSONObject("{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\"}]}}");

        MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

        assertNull(subject);
    }

    @Test
    public void create_putObjectWithEmptyAdConfiguration_returnEmptyObject() throws JSONException {
        if (!BuildConfig.DEBUG) {
            JSONObject jsonObject = new JSONObject(
                "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\", \"adconfiguration\":{}}]}}");

            MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

            assertNotNull(subject);

            assertNull(subject.isMuted);
            assertNull(subject.maxVideoDuration);
            assertNull(subject.skipDelay);
            assertNull(subject.skipButtonPosition);
            assertNull(subject.skipButtonArea);
            assertNull(subject.closeButtonArea);
            assertNull(subject.closeButtonPosition);
        }
    }

    @Test
    public void create_putObjectWithAdConfiguration_returnFullObject() throws JSONException {
        if (!BuildConfig.DEBUG) {

            JSONObject jsonObject = new JSONObject(
                "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\",\"adconfiguration\":{\n\"ismuted\": false,\n\"maxvideoduration\": 15,\n\"closebuttonarea\": 0.3,\n\"closebuttonposition\": \"topleft\",\n\"skipbuttonarea\": 0.3,\n\"skipbuttonposition\": \"topleft\",\n\"skipdelay\": 0}}]}}");

            MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

            assertNotNull(subject);

            assertEquals(false, subject.isMuted);
            assertEquals((Integer) 15, subject.maxVideoDuration);
            assertEquals((Integer) 0, subject.skipDelay);
            assertEquals(Position.TOP_LEFT, subject.skipButtonPosition);
            assertEquals((Double) 0.3, subject.skipButtonArea);
            assertEquals((Double) 0.3, subject.closeButtonArea);
            assertEquals(Position.TOP_LEFT, subject.closeButtonPosition);
        }
    }

    @Test
    public void modifyAdUnitConfiguration_putObjectWithAdConfiguration_getModifiedAdUnitConfiguration() throws JSONException {
        if (!BuildConfig.DEBUG) {

            JSONObject jsonObject = new JSONObject(
                "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\",\"adconfiguration\":{\n\"ismuted\": false,\n\"maxvideoduration\": 15,\n\"closebuttonarea\": 0.3,\n\"closebuttonposition\": \"topleft\",\n\"skipbuttonarea\": 0.3,\n\"skipbuttonposition\": \"topleft\",\n\"skipdelay\": 0}}]}}");

            MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);
            AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();

            assertNotNull(subject);

            subject.modifyAdUnitConfiguration(adUnitConfiguration);

            assertFalse(adUnitConfiguration.isMuted());
            assertEquals((Integer) 15, adUnitConfiguration.getMaxVideoDuration());
            assertEquals(0, adUnitConfiguration.getSkipDelay());
            assertEquals(Position.TOP_LEFT, adUnitConfiguration.getSkipButtonPosition());
            assertEquals(0.3, adUnitConfiguration.getSkipButtonArea(), 0);
            assertEquals(0.3, adUnitConfiguration.getCloseButtonArea(), 0);
            assertEquals(Position.TOP_LEFT, adUnitConfiguration.getCloseButtonPosition());
        }
    }

    @Test
    public void combine_checkFromBidPriority() throws JSONException {
        if (!BuildConfig.DEBUG) {
            JSONObject fromBidJsonObject = new JSONObject(
                "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\",\"adconfiguration\":{\n\"ismuted\": false,\n\"closebuttonarea\": 0.1}}]}}");
            JSONObject fromRootJsonObject = new JSONObject(
                "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\",\"adconfiguration\":{\n\"maxvideoduration\": 15,\n\"closebuttonarea\": 0.2}}]}}");
            MobileSdkPassThrough fromBid = MobileSdkPassThrough.create(fromBidJsonObject);
            MobileSdkPassThrough fromRoot = MobileSdkPassThrough.create(fromRootJsonObject);

            MobileSdkPassThrough result = MobileSdkPassThrough.combine(fromBid, fromRoot);

            assertNotNull(result);

            /* Only in fromBid response */
            assertFalse(result.isMuted);
            /* Only in fromRoot response */
            assertEquals((Integer) 15, result.maxVideoDuration);
            /* In fromBid = 0.1, in fromRoot = 0.2, fromBid have higher priority, so must be 0.1 */
            assertEquals((Double) 0.1, result.closeButtonArea);
        }
    }

    @Test
    public void create_putObjectWithSdkConfiguration_returnFullObject() throws JSONException {
        if (!BuildConfig.DEBUG) {

            JSONObject jsonObject = new JSONObject(
                    "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\", \n\"sdkconfiguration\": {\n\"cftbanner\": 7800, \n\"cftprerender\": 21000}}]}}");

            MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

            assertNotNull(subject);
            assertEquals((Integer) 7800, subject.bannerTimeout);
            assertEquals((Integer) 21000, subject.preRenderTimeout);

        }
    }

    @Test
    public void create_putObjectWithSdkConfiguration_onlyBannerTimeout() throws JSONException {
        if (!BuildConfig.DEBUG) {

            JSONObject jsonObject = new JSONObject(
                    "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\", \n\"sdkconfiguration\": {\n\"cftbanner\": 7900}}]}}");

            MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

            assertNotNull(subject);
            assertEquals((Integer) 7900, subject.bannerTimeout);
        }
    }

    @Test
    public void create_putObjectWithSdkConfiguration_onlyPreRenderTimeout() throws JSONException {
        if (!BuildConfig.DEBUG) {

            JSONObject jsonObject = new JSONObject(
                    "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\", \n\"sdkconfiguration\": {\n\"cftprerender\": 22000}}]}}");

            MobileSdkPassThrough subject = MobileSdkPassThrough.create(jsonObject);

            assertNotNull(subject);
            assertEquals((Integer) 22000, subject.preRenderTimeout);
        }
    }

}