package com.openx.apollo.utils.helpers;

import android.app.Activity;

import com.openx.apollo.sdk.ManagersResolver;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class RefreshTimerTaskTest {

    private RefreshTimerTask mRefreshTimerTask;

    private boolean mRefreshTriggered;

    @Before
    public void setup() {
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(testActivity);

        mRefreshTimerTask = new RefreshTimerTask(null);
    }

    @Test
    public void testIfRefreshTimerTriggeredAfterIntervalNoCallback() throws Exception {
        mRefreshTimerTask.scheduleRefreshTask(2000);
        //inject a delay to finish up post of message to the timertask.
        Thread.sleep(4000);

        //Because of NPE, this should return false
        assertFalse(mRefreshTimerTask.isRefreshExecuted());
    }

    @Test
    public void testIfRefreshTimerTriggeredAfterInterval() throws Exception {
        //No callback
        RefreshTimerTask refreshParams = new RefreshTimerTask(new RefreshTriggered() {
            @Override
            public void handleRefresh() {
                mRefreshTriggered = true;
            }
        });
        refreshParams.scheduleRefreshTask(2000);
        //inject a delay to finish up post of message to the timertask.
        Thread.sleep(5000);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertTrue(refreshParams.isRefreshExecuted());
        assertTrue(mRefreshTriggered);
    }

    @Ignore
    public void testIfRefreshTimerWorkedWhenIntervalNotExpired() {
        mRefreshTimerTask.scheduleRefreshTask(2000);
        //do not inject a delay to finish up post of message to the timertask.

        assertFalse(mRefreshTimerTask.isRefreshExecuted());
    }

    @Test
    public void testDestroyWhenNoTimerWasScheduled() {
        //refresh is not called as it was not scheduled at all.
        assertFalse(mRefreshTimerTask.isRefreshExecuted());

        mRefreshTimerTask.destroy();

        //refresh is set to false on destroy.
        assertFalse(mRefreshTimerTask.isRefreshExecuted());
    }

    @Test
    public void testDestroyWhenTimerWasScheduled() {
        mRefreshTimerTask.scheduleRefreshTask(1000);

        mRefreshTimerTask.destroy();

        //refresh is set to false on destroy.
        assertFalse(mRefreshTimerTask.isRefreshExecuted());
    }
}