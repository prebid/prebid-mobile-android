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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder;

public class VideoTest {

    @Test
    public void emptyVideoObject() throws JSONException {
        Video video = new Video();
        JSONObject json = video.getJsonObject();
        String expected = "{}";
        assertEquals(expected, json.toString());
    }

    @Test
    public void getJsonObject() throws Exception {
        Video video = new Video();
        video.mimes = BasicParameterBuilder.SUPPORTED_VIDEO_MIME_TYPES;
        video.minduration = 1;
        video.maxduration = 100;
        video.protocols = new int[]{2, 5};
        video.w = 300;
        video.h = 250;
        video.linearity = 1;
        video.minbitrate = 1;
        video.maxbitrate = 20;
        video.playbackmethod = new int[]{1, 2};
        video.delivery = new int[]{3};
        video.pos = 7;
        video.placement = 5;
        video.playbackend = 2;

        JSONObject actualObj = video.getJsonObject();
        String expectedString = "{\"delivery\":[3],\"linearity\":1,\"minbitrate\":1,\"h\":250,\"playbackmethod\":[1,2],\"minduration\":1,\"mimes\":[\"video/mp4\",\"video/3gpp\",\"video/webm\",\"video/mkv\"],\"maxbitrate\":20,\"maxduration\":100,\"playbackend\":2,\"pos\":7,\"w\":300,\"placement\":5,\"protocols\":[2,5]}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        video.getJsonObject();
    }

}