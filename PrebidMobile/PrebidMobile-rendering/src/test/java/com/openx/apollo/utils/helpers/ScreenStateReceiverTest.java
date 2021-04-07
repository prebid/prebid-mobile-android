package com.openx.apollo.utils.helpers;

import android.content.Context;
import android.content.Intent;

import com.openx.apollo.utils.broadcast.ScreenStateReceiver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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