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

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Banner;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Pmp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Video;

public class ImpTest {

    @Test
    public void getJsonObject() throws Exception {
        Imp imp = new Imp();

        imp.displaymanager = "prebid";
        imp.displaymanagerver = "1.0";
        imp.instl = 1;
        imp.tagid = "tagid";

        imp.secure = 0;
        imp.banner = new Banner();
        imp.video = new Video();
        imp.pmp = new Pmp();
        imp.clickBrowser = 0;

        JSONObject actualObj = imp.getJsonObject();
        String expectedString = "{\"clickbrowser\":0,\"pmp\":{},\"tagid\":\"tagid\",\"displaymanager\":\"prebid\",\"displaymanagerver\":\"1.0\",\"banner\":{},\"video\":{},\"secure\":0,\"instl\":1}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        imp.getJsonObject();
    }

    @Test
    public void impObjectWithFullVideo() throws JSONException {
        Imp imp = new Imp();

        imp.video = new Video();

        JSONObject actualObj = imp.getJsonObject();
        String expectedString = "{\"video\":{}}";
        assertEquals(expectedString, actualObj.toString());
        imp.getJsonObject();
    }

}