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

package org.prebid.mobile.addendum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Queue;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class LimitedQueueContainerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreationException() {
        //given
        int limit = -1;

        //when
        new LimitedQueueContainer<>(limit);
    }

    @Test
    public void testGetList() {
        //given
        int limit = 2;

        //when
        LimitedQueueContainer<Integer> limitedQueueContainer = new LimitedQueueContainer<>(limit);
        Queue<Integer> queue = limitedQueueContainer.getList();

        //then
        assertNotNull(queue);
    }

    @Test
    public void testAdd() {
        //given
        int limit = 2;

        //when
        LimitedQueueContainer<Integer> limitedQueueContainer = new LimitedQueueContainer<>(limit);

        limitedQueueContainer.add(1);
        limitedQueueContainer.add(2);
        limitedQueueContainer.add(3);

        //then
        assertEquals(limit, limitedQueueContainer.getList().size());
        assertTrue(limitedQueueContainer.getList().contains(2) && limitedQueueContainer.getList().contains(3));
    }

    @Test
    public void testIsFull() {
        //given
        int limit = 2;

        //when
        LimitedQueueContainer<Integer> limitedQueueContainer = new LimitedQueueContainer<>(limit);
        limitedQueueContainer.add(1);
        boolean result1 = limitedQueueContainer.isFull();
        limitedQueueContainer.add(2);
        boolean result2 = limitedQueueContainer.isFull();

        //then
        assertFalse(result1);
        assertTrue(result2);
    }
}