package org.prebid.mobile.rendering.models.openrtb.bidRequests.apps;

import com.apollo.test.utils.ResourceUtils;

import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;

/**
 * Created by latha.shivanna on 11/2/17.
 */
public class PublisherTest {

    public final static String EXPECTED_PUBLISHER = "publisher_expected.txt";

    @Test
    public void getJsonObjectTest() throws Exception {
        File myFile = new File(EXPECTED_PUBLISHER);
        System.out.println(myFile.getAbsolutePath());

        Publisher publisher = new Publisher();
        publisher.name = "blah";
      
        String[] cat = new String[1];
        cat[0] = "IAB2-2";
        publisher.cat = cat;
        publisher.domain  = "test.domain.com";

        JSONObject actualObj = publisher.getJsonObject();
        String expectedString = ResourceUtils.convertResourceToString(EXPECTED_PUBLISHER);
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        publisher.getJsonObject();
    }
}