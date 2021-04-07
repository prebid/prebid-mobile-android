package com.openx.apollo.sdk;

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
    private JSLibraryManager mManager;

    @Before
    public void setup() throws Exception {
        Context mock = mock(Context.class);
        when(mock.getApplicationContext()).thenReturn(mock);
        when(mock.getResources()).thenReturn(mock(Resources.class));

        mManager = Mockito.spy(JSLibraryManager.getInstance(mock));
    }

    @Test
    public void testInitialStrings() {
        Assert.assertEquals("", mManager.getMRAIDScript());
        Assert.assertEquals("", mManager.getOMSDKScript());
    }
}