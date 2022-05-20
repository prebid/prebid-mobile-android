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
import android.os.Bundle;
import android.provider.CalendarContract;
import org.json.JSONObject;
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

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CalendarGTE14Test {
    private final static String CALENDAR_FILE = "calendar.txt";
    private final static String CALENDAR_FILE_CHECK_NULL_VALUES = "calendarCheckNullValues.txt";

    private static final String CALENDAR_RECURRENCE_DAILY = "calendarRecurrenceDaily.txt";
    private static final String CALENDAR_RECURRENCE_MONTHLY = "calendarRecurrenceMonthly.txt";
    private static final String CALENDAR_RECURRENCE_WEEKLY = "calendarRecurrenceWeekly.txt";

    private static final String CALENDAR_RECURRENCE_YEARLY_NO_DETAILS = "calendarRecurrenceYearlyNoDetails.txt";
    private static final String CALENDAR_RECURRENCE_YEARLY_DETAILED = "calendarRecurrenceYearlyDetailed.txt";
    private static final String CALENDAR_RECURRENCE_YEARLY_EMPTY_WEEK_IN_MONTH = "calendarRecurrenceYearlyEmptyWeekInMonth.txt";

    private Activity testActivity;

    @Before
    public void setUp() throws Exception {
        testActivity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void testCreateCalendarEvent() throws Exception {
        init(CALENDAR_FILE);
        Intent startedIntent = getNextStartedActivityIntent();
        Bundle extras = startedIntent.getExtras();

        assertEquals("vnd.android.cursor.item/event", startedIntent.getType());
        assertFalse("fail.event.type".equals(startedIntent.getType()));

        assertEquals("", extras.get(CalendarContract.Events.TITLE));

        assertEquals("mayanApocalypse/End of world", extras.get(CalendarContract.Events.DESCRIPTION));

        assertEquals("everywhere", extras.get(CalendarContract.Events.EVENT_LOCATION));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        assertEquals("2012-12-21 00:00:05", dateFormat.format(new Date((Long) startedIntent.getExtras().get(CalendarContract.EXTRA_EVENT_BEGIN_TIME))));
        assertEquals("2012-12-22 00:05:00", dateFormat.format(new Date((Long) startedIntent.getExtras().get(CalendarContract.EXTRA_EVENT_END_TIME))));

        assertFalse(extras.getBoolean(CalendarContract.EXTRA_EVENT_ALL_DAY));

        assertEquals(0, extras.getInt(CalendarContract.Events.ACCESS_LEVEL));

        assertEquals(1, extras.getInt(CalendarContract.Events.AVAILABILITY));

        assertFalse(Boolean.parseBoolean(extras.getString(CalendarContract.Events.HAS_ALARM)));

        assertEquals("FREQ=DAILY;INTERVAL=1",
                     extras.getString(CalendarContract.Events.RRULE));
    }

    @Test
    public void testCreateCalendarWithNullValues() throws Exception {
        init(CALENDAR_FILE_CHECK_NULL_VALUES);
        Bundle extra = getNextStartedActivityIntent().getExtras();
        assertEquals("", extra.get(CalendarContract.Events.EVENT_LOCATION));
        assertEquals("", extra.get(CalendarContract.Events.DESCRIPTION));
        assertTrue(extra.getBoolean(CalendarContract.Events.HAS_ALARM));
    }

    @Test
    public void testDailyRecurrence() throws Exception {
        init(CALENDAR_RECURRENCE_DAILY);
        assertEquals("FREQ=DAILY;INTERVAL=2;BYDAY=SU,MO,TU",
                     getNextStartedActivityExtraCalendarRule());
    }

    @Test
    public void testMonthlyRecurrence() throws Exception {
        init(CALENDAR_RECURRENCE_MONTHLY);
        assertEquals("FREQ=MONTHLY;INTERVAL=1;BYDAY=TU,WE,TH",
                     getNextStartedActivityExtraCalendarRule());
    }

    @Test
    public void testWeeklyRecurrence() throws Exception {
        init(CALENDAR_RECURRENCE_WEEKLY);
        assertEquals("FREQ=WEEKLY;INTERVAL=3;BYDAY=FR,SA",
                     getNextStartedActivityExtraCalendarRule());
    }

    @Test
    public void testRecurrenceYearlyNoDetails() throws Exception {
        init(CALENDAR_RECURRENCE_YEARLY_NO_DETAILS);
        assertEquals("FREQ=YEARLY;INTERVAL=1",
                     getNextStartedActivityExtraCalendarRule());
    }

    @Test
    public void testRecurrenceYearlyDetailed() throws Exception {
        init(CALENDAR_RECURRENCE_YEARLY_DETAILED);
        assertEquals("FREQ=YEARLY;INTERVAL=1;BYDAY=MO,TU;BYMONTHDAY=10;BYYEARDAY=23;BYMONTH=1;BYWEEKNO=2",
                     getNextStartedActivityExtraCalendarRule());
    }

    @Test
    public void testRecurrenceYearlyEmptyWeekInMonth() throws Exception {
        init(CALENDAR_RECURRENCE_YEARLY_EMPTY_WEEK_IN_MONTH);
        assertEquals("FREQ=YEARLY;INTERVAL=1",
                     getNextStartedActivityExtraCalendarRule());
    }

    private void init(String resourceFile) throws Exception {
        CalendarGTE14 calendar = new CalendarGTE14();
        String json = ResourceUtils.convertResourceToString(resourceFile);

        JSONObject jsonObj = new JSONObject(json);

        CalendarEventWrapper event = new CalendarEventWrapper(jsonObj);
        calendar.createCalendarEvent(testActivity, event);
    }

    private String getNextStartedActivityExtraCalendarRule() {
        return getNextStartedActivityIntent().getExtras().getString(CalendarContract.Events.RRULE);
    }

    private Intent getNextStartedActivityIntent() {
        ShadowActivity shadowActivity = shadowOf(testActivity);
        return shadowActivity.getNextStartedActivity();
    }
}