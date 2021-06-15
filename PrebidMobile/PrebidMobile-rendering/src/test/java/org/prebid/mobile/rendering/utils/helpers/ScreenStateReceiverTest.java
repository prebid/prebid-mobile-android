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

package org.prebid.mobile.rendering.utils.helpers;

import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScreenStateReceiverTest {
    private ScreenStateReceiver mScreenStateReceiver;

    @Mock
    private Context mMockContext;
    @Mock
    private Intent mMockIntent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mScreenStateReceiver = new ScreenStateReceiver();
    }

    @Test
    public void onReceiveActionUserPresent_ScreenOnIsTrue() {
        when(mMockIntent.getAction()).thenReturn(Intent.ACTION_USER_PRESENT);

        mScreenStateReceiver.onReceive(mMockContext, mMockIntent);

        assertTrue(mScreenStateReceiver.isScreenOn());
    }

    @Test
    public void onReceiveActionScreenOff_ScreenOnIsFalse() {
        when(mMockIntent.getAction()).thenReturn(Intent.ACTION_SCREEN_OFF);

        mScreenStateReceiver.onReceive(mMockContext, mMockIntent);

        assertFalse(mScreenStateReceiver.isScreenOn());
    }

    @Test
    public void whenRegisterAndUnregisterReceiver_PerformActionOnContext() {
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);

        mScreenStateReceiver.register(mMockContext);

        verify(mMockContext, times(1)).getApplicationContext();
        verify(mMockContext, times(1)).registerReceiver(eq(mScreenStateReceiver), any());

        mScreenStateReceiver.unregister();
        verify(mMockContext, times(1)).unregisterReceiver(eq(mScreenStateReceiver));
    }
}