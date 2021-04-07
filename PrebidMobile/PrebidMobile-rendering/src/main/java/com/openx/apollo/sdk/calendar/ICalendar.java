package com.openx.apollo.sdk.calendar;

import android.content.Context;


/**
 * The interface gives access to internal calendar implementation which allows to create event through different
 * Android SDK versions
 */
public interface ICalendar {
    void createCalendarEvent(Context context, CalendarEventWrapper event);
}