/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class VideoUtilsTest extends BaseSetup {

    @Test
    public void testBuildAdTagUrlByMap() throws Exception {
        Map<String, String> targetingMap = new HashMap<>(2);
        targetingMap.put("key1", "value1");
        targetingMap.put("key2", "value2");

        String adTagUrl = VideoUtils.buildAdTagUrl("adUnitId", "300x250", targetingMap);
        assertTrue(adTagUrl.contains("https://pubads.g.doubleclick.net/gampad/ads?env=vp&gdfp_req=1&unviewed_position_start=1&output=xml_vast4&vpmute=1&iu=adUnitId&sz=300x250&cust_params=key1%3Dvalue1%26key2%3Dvalue2"));
    }

}
