package com.openx.apollo.sdk.calendar;

import com.openx.apollo.utils.helpers.Utils;

final public class CalendarFactory {

    private ICalendar mImplementation;

    private CalendarFactory() {
        if (Utils.atLeastICS()) {
            mImplementation = new CalendarGTE14();
        }
        else {
            mImplementation = new CalendarLT14();
        }
    }

    private static class CalendarImplHolder {
        public static final CalendarFactory instance = new CalendarFactory();
    }

    public static ICalendar getCalendarInstance() {
        return CalendarImplHolder.instance.mImplementation;
    }
}