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

package org.prebid.mobile.rendering.sdk.calendar;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CalendarLT14Test {

    private final static String CALENDAR_FILE = "calendar.txt";
    private Activity testActivity;

    @Before
    public void setUp() throws Exception {
        testActivity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateCalendarEvent() throws Exception {
        CalendarLT14 calendar = new CalendarLT14();
        String json = ResourceUtils.convertResourceToString(CALENDAR_FILE);

        JSONObject jsonObj = new JSONObject(json);
        CalendarEventWrapper event = new CalendarEventWrapper(jsonObj);
        calendar.createCalendarEvent(testActivity, event);

        ShadowActivity shadowActivity = shadowOf(testActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals("vnd.android.cursor.item/event", startedIntent.getType());
        assertFalse("fail.event.type".equals(startedIntent.getType()));
        assertEquals("", startedIntent.getExtras().get(CalendarContract.Events.TITLE));
        assertEquals("mayanApocalypse/End of world", startedIntent.getExtras().get(CalendarContract.Events.DESCRIPTION));

        assertEquals("everywhere", startedIntent.getExtras().get(CalendarContract.Events.EVENT_LOCATION));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        assertEquals("2012-12-21 00:00:05", dateFormat.format(new Date((Long) startedIntent.getExtras().get(CalendarContract.EXTRA_EVENT_BEGIN_TIME))));
        assertEquals("2012-12-22 00:05:00", dateFormat.format(new Date((Long) startedIntent.getExtras().get(CalendarContract.EXTRA_EVENT_END_TIME))));


        assertFalse(startedIntent.getExtras().getBoolean(CalendarContract.EXTRA_EVENT_ALL_DAY));


        assertFalse(Boolean.parseBoolean(startedIntent.getExtras().getString("visibility")));

        assertFalse(Boolean.parseBoolean(startedIntent.getExtras().getString(CalendarContract
                                                                                 .Events.HAS_ALARM)));

    }
}