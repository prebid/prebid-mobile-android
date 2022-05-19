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

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import static org.junit.Assert.*;

public class ICalendarRepeatRuleTest {

    private static final String CALENDAR_FILE_WITH_EXCEPTION_DATES = "calendarRepeatRuleWithExceptionDates.txt";
    private static final String CALENDAR_FILE = "calendarRepeatRule.txt";

    private CalendarRepeatRule calendarRule;

    @Before
    public void setUp() throws Exception {
        String json = ResourceUtils.convertResourceToString(CALENDAR_FILE);
        JSONObject jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
    }

    @After
    public void tearDown() {
        calendarRule = null;
    }

    @Test
    public void testSetExpires() {
        calendarRule.setExpires("expires: '2011-06-11T12:00:00-04:00'}");
        assertNotNull(calendarRule.getExpires());
    }

    @Test
    public void testGetDaysInWeek() {
        assertTrue(calendarRule.getDaysInWeek().length == 2);
        assertTrue(calendarRule.getDaysInWeek()[0] == 1);
    }

    @Test
    public void testGetDaysInMonth() {
        assertTrue(calendarRule.getDaysInMonth().length == 1);
        assertTrue(calendarRule.getDaysInMonth()[0] == 10);
    }

    @Test
    public void testDaysInYear() {
        assertTrue(calendarRule.getDaysInYear().length == 1);
        assertTrue(calendarRule.getDaysInYear()[0] == 23);
    }

    @Test
    public void testFailedExpires() {
        calendarRule.setExpires("bbbnbnbn");
        DateWrapper actual = calendarRule.getExpires();
        assertFalse(actual.isEmpty());
        assertNull(actual.getDate());
        assertNull(actual.getTimeZone());
    }

    @Test
    public void testExceptionDates() throws Exception {
        String json = ResourceUtils.convertResourceToString(CALENDAR_FILE_WITH_EXCEPTION_DATES);
        JSONObject jsonObj = new JSONObject(json);
        CalendarRepeatRule calendarRule = new CalendarRepeatRule(jsonObj);
        DateWrapper[] actualDates = calendarRule.getExceptionDates();

        assertNotNull(actualDates);
        assertEquals(2, actualDates.length);
        assertNotEquals(1, actualDates.length);

        DateWrapper date1 = new DateWrapper("2011-05-05T12:00Z");
        DateWrapper date2 = new DateWrapper("2011-05-06T09:11Z");
        DateWrapper[] expectedDates = new DateWrapper[]{date1, date2};
        assertArrayEquals(expectedDates, actualDates);

        calendarRule.setExceptionDates(new String[]{"2018"});
        assertNull(calendarRule.getExceptionDates()[0]);
    }

    @Test
    public void testFrequency() throws Exception {
        String json = "{\"frequency\": \"daily\"}";
        JSONObject jsonObj = new JSONObject(json);
        CalendarRepeatRule calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.DAILY, calendarRule.getFrequency());

        json = "{\"frequency\": \"weekly\"}";
        jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.WEEKLY, calendarRule.getFrequency());

        json = "{\"frequency\": \"monthly\"}";
        jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.MONTHLY, calendarRule.getFrequency());

        json = "{\"frequency\": \"yearly\"}";
        jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.YEARLY, calendarRule.getFrequency());

        json = "{\"frequency\": \"secondly\"}";
        jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.UNKNOWN, calendarRule.getFrequency());

        json = "{\"frequency\": \"\"}";
        jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.UNKNOWN, calendarRule.getFrequency());

        json = "{\"interval\": \"2\"}";
        jsonObj = new JSONObject(json);
        calendarRule = new CalendarRepeatRule(jsonObj);
        assertEquals(CalendarRepeatRule.Frequency.UNKNOWN, calendarRule.getFrequency());
    }

    @Test
    public void tesInterval() throws Exception {
        assertTrue(calendarRule.getInterval() == 2);
        calendarRule.setInterval(3);
        assertEquals(new Integer(3), calendarRule.getInterval());
    }

    @Test
    public void testMonthsInYear() {
        assertTrue(calendarRule.getMonthsInYear().length == 1);
        assertTrue(calendarRule.getMonthsInYear()[0] == 1);

        Short[] expected = new Short[]{new Short("1")};
        calendarRule.setMonthsInYear(expected);
        assertTrue(calendarRule.getMonthsInYear().length == 1);
        assertEquals(expected, calendarRule.getMonthsInYear());
    }

    @Test
    public void testWeeksInMonth() {
        assertTrue(calendarRule.getWeeksInMonth().length == 1);
        assertTrue(calendarRule.getWeeksInMonth()[0] == 2);

        Short[] expected = new Short[]{new Short("1")};
        calendarRule.setWeeksInMonth(expected);
        assertTrue(calendarRule.getWeeksInMonth().length == 1);
        assertEquals(expected, calendarRule.getWeeksInMonth());
    }
}