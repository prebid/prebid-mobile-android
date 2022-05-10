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

package org.prebid.mobile.rendering.bidding.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdSize;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdSizeTest {

    @Test
    public void whenInit_FieldsAssignedCorrectly() {
        AdSize adSize = new AdSize(1, 2);
        assertEquals(adSize.getWidth(), 1);
        assertEquals(adSize.getHeight(), 2);
    }

    @Test
    public void whenFieldsAreEqual_ObjectsAreEqual() {
        AdSize adSize = new AdSize(1, 1);
        AdSize newAdSize = new AdSize(1, 1);
        assertEquals(adSize, newAdSize);
    }
}