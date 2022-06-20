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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.apps;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;

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

