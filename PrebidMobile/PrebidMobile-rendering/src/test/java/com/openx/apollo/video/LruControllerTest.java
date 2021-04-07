package com.openx.apollo.video;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class LruControllerTest {

    private String mUrl = "http://path/to/video/Vast_Video.mp4";

    @Test
    public void whenNoAdUnits_UseVideoNameOnly() {
        assertEquals("/Vast_Video", LruController.getShortenedPath(mUrl));
    }
}