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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.rendering.utils.broadcast.OrientationBroadcastReceiver;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class OrientationBroadcastReceiverTest {

    private OrientationBroadcastReceiver mOrientationReceiver;
    private Context mMockContext;

    @Before
    public void setup() {
        mOrientationReceiver = Mockito.spy(new OrientationBroadcastReceiver());
        mMockContext = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void onReceiveTest() throws IllegalAccessException {
        Intent intent = new Intent();
        Field contextField = WhiteBox.field(OrientationBroadcastReceiver.class, "mApplicationContext");

        contextField.set(mOrientationReceiver, mMockContext.getApplicationContext());
        intent.setAction(Intent.ACTION_SCREEN_ON);
        mOrientationReceiver.onReceive(null, intent);
        verify(mOrientationReceiver, times(0)).setOrientationChanged(anyBoolean());

        intent.setAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mOrientationReceiver.onReceive(null, intent);
        verify(mOrientationReceiver, times(1)).setOrientationChanged(eq(true));
        verify(mOrientationReceiver, times(1)).handleOrientationChange(anyInt());

        mOrientationReceiver.onReceive(null, intent);
        verify(mOrientationReceiver, times(1)).setOrientationChanged(eq(false));
    }

    @Test
    public void registerTest() {
        mMockContext = spy(mMockContext);
        Context mockContext = mock(Context.class);
        when(mockContext.getApplicationContext()).thenReturn(mMockContext);

        mOrientationReceiver.register(mockContext);
        verify(mMockContext).registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void unregisterTest() throws IllegalAccessException {
        mMockContext = spy(mMockContext);
        doNothing().when(mMockContext).unregisterReceiver(any(BroadcastReceiver.class));
        Field contextField = WhiteBox.field(OrientationBroadcastReceiver.class, "mApplicationContext");
        contextField.set(mOrientationReceiver, mMockContext);

        mOrientationReceiver.unregister();
        verify(mMockContext).unregisterReceiver(any(BroadcastReceiver.class));
        Assert.assertNull(contextField.get(mOrientationReceiver));
    }
}