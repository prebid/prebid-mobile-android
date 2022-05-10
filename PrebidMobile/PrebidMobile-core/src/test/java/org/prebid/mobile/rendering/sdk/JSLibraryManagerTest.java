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

package org.prebid.mobile.rendering.sdk;

import android.content.Context;
import android.content.res.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class JSLibraryManagerTest {
    private JSLibraryManager manager;

    @Before
    public void setup() throws Exception {
        Context mock = mock(Context.class);
        when(mock.getApplicationContext()).thenReturn(mock);
        when(mock.getResources()).thenReturn(mock(Resources.class));

        manager = Mockito.spy(JSLibraryManager.getInstance(mock));
    }

    @Test
    public void testInitialStrings() {
        Assert.assertEquals("", manager.getMRAIDScript());
        Assert.assertEquals("", manager.getOMSDKScript());
    }
}