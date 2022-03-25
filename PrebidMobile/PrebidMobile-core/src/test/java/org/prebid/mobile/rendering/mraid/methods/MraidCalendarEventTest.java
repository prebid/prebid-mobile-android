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

package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidCalendarEventTest {

    public final static String mCalendarFile = "calendar.txt";
    private String mCalendarParameters = null;
    private Activity mTestActivity;

    @Before
    public void setUp() throws Exception {
        mTestActivity = Robolectric.buildActivity(Activity.class).create().get();
        mCalendarParameters = ResourceUtils.convertResourceToString(mCalendarFile);
        ManagersResolver.getInstance().prepare(mTestActivity);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateCalendarEvent() {
        BaseJSInterface mockJs = mock(BaseJSInterface.class);
        MraidCalendarEvent event = new MraidCalendarEvent(mockJs);

        event.createCalendarEvent(mCalendarParameters);
        ShadowActivity shadowActivity = shadowOf(mTestActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertFalse("somecrap.calendar/event".equals(startedIntent.getType()));
        assertEquals("vnd.android.cursor.item/event", startedIntent.getType());
        assertFalse("fail.event.type".equals(startedIntent.getType()));
        assertEquals("", startedIntent.getExtras().get(CalendarContract.Events.TITLE));
        assertTrue(startedIntent.getExtras().get(CalendarContract.Events.DESCRIPTION).equals
            ("mayanApocalypse/End of world"));
    }
}