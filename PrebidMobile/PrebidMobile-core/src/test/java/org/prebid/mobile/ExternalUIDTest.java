/*
 *    Copyright 2021 Prebid.org, Inc.
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

package org.prebid.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class ExternalUIDTest extends BaseSetup {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Override
    public void setup() {
        super.setup();
        PrebidMobile.initializeSdk(activity.getApplicationContext(), null);
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void testExternalUserIdsConversion() {
        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
        externalUserIdArray.add(new ExternalUserId("", "111111111111", null, new HashMap() {{ put("rtiPartner", "TDID");}}));
        externalUserIdArray.add(new ExternalUserId("netid.de", "", null, null));
        externalUserIdArray.add(new ExternalUserId(null , "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
        externalUserIdArray.add(new ExternalUserId("liveramp.com", null, null, null));
        externalUserIdArray.add(new ExternalUserId("sharedid.org", "111111111111", 1, new HashMap() {{ put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");}}));
        externalUserIdArray.add(new ExternalUserId(null, null, null, null));
        PrebidMobile.setExternalUserIds(externalUserIdArray);

        System.out.println("EXTERUSERID: " + externalUserIdArray);
        System.out.println("EXTERUSERID: " + ExternalUserId.getExternalUidListFromJson(externalUserIdArray.toString()));
        System.out.println("EXTERUSERID: CONVERTED: " + ExternalUserId.getExternalUidListFromJson(externalUserIdArray.toString()).toString().equals(externalUserIdArray.toString()));
        assertTrue(ExternalUserId.getExternalUidListFromJson(externalUserIdArray.toString()).toString().equals(externalUserIdArray.toString()));
    }
}
