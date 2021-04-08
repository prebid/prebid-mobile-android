package org.prebid.mobile.rendering.models.openrtb.bidRequests.apps;

import com.apollo.test.utils.ResourceUtils;

import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;

public class ContentTest {

    public final static String CONTENT_EXPECTED = "content_expected.txt";

    @Test
    public void getJsonObjectTest() throws Exception {
        File myFile = new File(CONTENT_EXPECTED);
        System.out.println(myFile.getAbsolutePath());

        Content content = new Content();
        content.id = "1234567893-2";
        content.series = "AllAboutCars";
        content.season = "2";
        content.episode = 23;
        content.title = "CarShow";
        String[] cat = new String[1];
        cat[0] = "IAB2-2";
        content.cat = cat;
        content.keywords = "keyword-a, keyword-b, keyword-c";

        JSONObject actualObj = content.getJsonObject();
        String expectedString = ResourceUtils.convertResourceToString(CONTENT_EXPECTED);
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        content.getJsonObject();
    }
}

