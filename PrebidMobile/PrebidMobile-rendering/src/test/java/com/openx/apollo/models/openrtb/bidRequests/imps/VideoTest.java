package com.openx.apollo.models.openrtb.bidRequests.imps;

import com.openx.apollo.networking.parameters.BasicParameterBuilder;

import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class VideoTest {
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