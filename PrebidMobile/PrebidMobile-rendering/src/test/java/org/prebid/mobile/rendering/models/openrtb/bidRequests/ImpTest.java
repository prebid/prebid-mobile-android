package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Banner;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Pmp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Video;

import static junit.framework.Assert.assertEquals;

public class ImpTest {
    @Test
    public void getJsonObject() throws Exception {
        Imp imp = new Imp();

        imp.displaymanager = "openx";
        imp.displaymanagerver = "1.0";
        imp.instl = 1;
        imp.tagid = "tagid";

        imp.secure = 0;
        imp.banner = new Banner();
        imp.video = new Video();
        imp.pmp = new Pmp();
        imp.clickBrowser = 0;

        JSONObject actualObj = imp.getJsonObject();
        String expectedString = "{\"clickbrowser\":0,\"pmp\":{},\"tagid\":\"tagid\",\"displaymanager\":\"openx\",\"displaymanagerver\":\"1.0\",\"banner\":{},\"video\":{},\"secure\":0,\"instl\":1}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        imp.getJsonObject();
    }
}