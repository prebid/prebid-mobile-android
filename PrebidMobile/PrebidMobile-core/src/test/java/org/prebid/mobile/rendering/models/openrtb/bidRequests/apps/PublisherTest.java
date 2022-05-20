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