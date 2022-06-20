/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.ContentObject;

import static org.junit.Assert.assertEquals;

public class AppTest {
    @Test
    public void getJsonObject() throws Exception {
        App app = new App();

        app.id = "appId";
        app.name = "test";
        app.bundle = "com.test.test";
        app.domain = "com.us.prebid.net";

        app.storeurl = "test.url.com";
        app.cat = new String[]{"1", "2"};
        app.sectioncat = new String[]{"1", "2"};
        app.pagecat = new String[]{"1", "2"};
        app.ver = "1";
        app.privacypolicy = 1;
        app.paid = 1;
        app.keywords = "blah, blah";
        app.getPublisher().name = "name";

        ContentObject contentObject = new ContentObject();
        contentObject.setUrl("test.content.com");
        app.contentObject = contentObject;

        JSONObject actualObj = app.getJsonObject();
        String expectedString = "{\"ver\":\"1\",\"privacypolicy\":1,\"keywords\":\"blah, blah\",\"content\":{\"url\":\"test.content.com\"},\"sectioncat\":[\"1\",\"2\"],\"storeurl\":\"test.url.com\",\"domain\":\"com.us.prebid.net\",\"cat\":[\"1\",\"2\"],\"name\":\"test\",\"paid\":1,\"publisher\":{\"name\":\"name\"},\"id\":\"appId\",\"bundle\":\"com.test.test\",\"pagecat\":[\"1\",\"2\"]}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
    }
}