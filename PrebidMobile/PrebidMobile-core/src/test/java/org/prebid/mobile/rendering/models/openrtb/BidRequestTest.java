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

package org.prebid.mobile.rendering.models.openrtb;

import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class BidRequestTest {

    @Test
    public void getJsonObject() throws Exception {
        BidRequest bidReq = new BidRequest();

        App app = new App();
        app.id = "auid";
        bidReq.setApp(app);
        Device device = new Device();
        device.h = 1111;
        bidReq.setDevice(device);

        Imp imp = new Imp();
        imp.instl = 0;
        ArrayList<Imp> imps = new ArrayList<>();
        imps.add(imp);
        bidReq.setImp(imps);
        Regs regs = new Regs();
        regs.coppa = 0;
        bidReq.setRegs(regs);

        User user = new User();
        user.keywords = "q, o";
        bidReq.setUser(user);

        JSONObject actualObj = bidReq.getJsonObject();
        String expectedString = "{\"app\":{\"id\":\"auid\"},\"regs\":{\"coppa\":0},\"imp\":[{\"instl\":0}],\"device\":{\"h\":1111},\"user\":{\"keywords\":\"q, o\"}}";
        assertEquals("got: " + actualObj.toString(), expectedString, actualObj.toString());
        bidReq.getJsonObject();
    }
}