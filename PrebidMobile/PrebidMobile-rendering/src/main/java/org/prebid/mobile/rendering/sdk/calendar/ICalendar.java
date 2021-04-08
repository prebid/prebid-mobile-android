package org.prebid.mobile.rendering.sdk.calendar;

import android.content.Context;


/**
 * The interface gives access to internal calendar implementation which allows to create event through different
 * Android SDK versions
 */
public interface ICalendar {
    void createCalendarEvent(Context context, CalendarEventWrapper event);
}