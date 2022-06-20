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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class RefreshTimerTaskTest {

    private RefreshTimerTask refreshTimerTask;

    private boolean refreshTriggered;

    @Before
    public void setup() {
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(testActivity);

        refreshTimerTask = new RefreshTimerTask(null);
    }

    @Test
    public void testIfRefreshTimerTriggeredAfterIntervalNoCallback() throws Exception {
        refreshTimerTask.scheduleRefreshTask(2000);
        //inject a delay to finish up post of message to the timertask.
        Thread.sleep(4000);

        //Because of NPE, this should return false
        assertFalse(refreshTimerTask.isRefreshExecuted());
    }

    @Test
    public void testIfRefreshTimerTriggeredAfterInterval() throws Exception {
        //No callback
        RefreshTimerTask refreshParams = new RefreshTimerTask(new RefreshTriggered() {
            @Override
            public void handleRefresh() {
                refreshTriggered = true;
            }
        });
        refreshParams.scheduleRefreshTask(2000);
        //inject a delay to finish up post of message to the timertask.
        Thread.sleep(5000);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertTrue(refreshParams.isRefreshExecuted());
        assertTrue(refreshTriggered);
    }

    @Ignore
    public void testIfRefreshTimerWorkedWhenIntervalNotExpired() {
        refreshTimerTask.scheduleRefreshTask(2000);
        //do not inject a delay to finish up post of message to the timertask.

        assertFalse(refreshTimerTask.isRefreshExecuted());
    }

    @Test
    public void testDestroyWhenNoTimerWasScheduled() {
        //refresh is not called as it was not scheduled at all.
        assertFalse(refreshTimerTask.isRefreshExecuted());

        refreshTimerTask.destroy();

        //refresh is set to false on destroy.
        assertFalse(refreshTimerTask.isRefreshExecuted());
    }

    @Test
    public void testDestroyWhenTimerWasScheduled() {
        refreshTimerTask.scheduleRefreshTask(1000);

        refreshTimerTask.destroy();

        //refresh is set to false on destroy.
        assertFalse(refreshTimerTask.isRefreshExecuted());
    }
}