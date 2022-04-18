package org.prebid.mobile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ParametersMatcherTest {

    @Test
    public void putBlankPrimaryParameters_ReturnsFalse() {
        String adMobParameters = " ";
        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("a", "a1");

        assertFalse(ParametersMatcher.doParametersMatch(adMobParameters, prebidParameters));
    }

    @Test
    public void putNullPrimaryParameters_ReturnsFalse() {
        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("a", "a1");

        assertFalse(ParametersMatcher.doParametersMatch((String) null, prebidParameters));
    }

    @Test
    public void putNullPrebidParameters_ReturnsFalse() {
        String adMobParameters = "Any text";

        assertFalse(ParametersMatcher.doParametersMatch(adMobParameters, null));
    }

    @Test
    public void primaryParametersContainsKeyThatNotExistInPrebid_ReturnsFalse() {
        String primaryParameters = "{\"hb_pb\":\"0.10\",\"any\":\"10\"}";

        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("hb_pb_pi", "10");
        prebidParameters.put("hb_pb", "0.10");
        prebidParameters.put("hb_bidder_pi", "PI");

        assertFalse(ParametersMatcher.doParametersMatch(primaryParameters, prebidParameters));
    }

    @Test
    public void putWrongPrice_ReturnsFalse() {
        String primaryParameters = "{\"hb_pb\":\"1.00\"}";

        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("hb_pb_pi", "10");
        prebidParameters.put("hb_pb", "0.10");
        prebidParameters.put("hb_bidder_pi", "PI");

        assertFalse(ParametersMatcher.doParametersMatch(primaryParameters, prebidParameters));
    }

    @Test
    public void putCorrectParameters_ReturnsTrue() {
        String primaryParameters = "{\"hb_pb\":\"0.10\"}";

        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("hb_pb_pi", "10");
        prebidParameters.put("hb_pb", "0.10");
        prebidParameters.put("hb_bidder_pi", "PI");

        assertTrue(ParametersMatcher.doParametersMatch(primaryParameters, prebidParameters));
    }

    @Test
    public void putMultipleCorrectParameters_ReturnsTrue() {
        String primaryParameters = "{\"hb_pb\":\"0.10\",\"hb_bidder_pi\":\"PI\"}";

        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("hb_pb_pi", "10");
        prebidParameters.put("hb_pb", "0.10");
        prebidParameters.put("hb_bidder_pi", "PI");

        assertTrue(ParametersMatcher.doParametersMatch(primaryParameters, prebidParameters));
    }

    @Test
    public void putRightAndWrongParameters_ReturnsFalse() {
        String primaryParameters = "{\"hb_pb\":\"0.10\",\"hb_bidder_pi\":\"prebid\"}";

        HashMap<String, String> prebidParameters = new HashMap<>();
        prebidParameters.put("hb_pb_pi", "10");
        prebidParameters.put("hb_pb", "0.10");
        prebidParameters.put("hb_bidder_pi", "PI");

        assertFalse(ParametersMatcher.doParametersMatch(primaryParameters, prebidParameters));
    }

}
