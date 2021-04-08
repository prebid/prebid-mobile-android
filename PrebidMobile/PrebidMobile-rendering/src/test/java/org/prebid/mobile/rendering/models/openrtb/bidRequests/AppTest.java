package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class AppTest {
    @Test
    public void getJsonObject() throws Exception {
        App app = new App();

        app.id = "appId";
        app.name = "test";
        app.bundle = "com.test.test";
        app.domain = "com.us.openx.net";

        app.storeurl = "test.url.com";
        app.cat = new String[]{"1", "2"};
        app.sectioncat = new String[]{"1", "2"};
        app.pagecat = new String[]{"1", "2"};
        app.ver = "1";
        app.privacypolicy = 1;
        app.paid = 1;
        app.keywords = "blah, blah";
        app.getPublisher().name = "name";

        JSONObject actualObj = app.getJsonObject();
        String expectedString = "{\"ver\":\"1\",\"privacypolicy\":1,\"keywords\":\"blah, blah\",\"sectioncat\":[\"1\",\"2\"],\"storeurl\":\"test.url.com\",\"domain\":\"com.us.openx.net\",\"cat\":[\"1\",\"2\"],\"name\":\"test\",\"paid\":1,\"publisher\":{\"name\":\"name\"},\"id\":\"appId\",\"bundle\":\"com.test.test\",\"pagecat\":[\"1\",\"2\"]}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
    }
}