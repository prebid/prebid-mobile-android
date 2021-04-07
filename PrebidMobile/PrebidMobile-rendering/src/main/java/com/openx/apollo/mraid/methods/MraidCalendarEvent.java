package com.openx.apollo.mraid.methods;

import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.sdk.calendar.CalendarEventWrapper;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;

import org.json.JSONObject;

public class MraidCalendarEvent {

    private BaseJSInterface mJsi;

    public MraidCalendarEvent(BaseJSInterface jsInterface) {
        this.mJsi = jsInterface;
    }

    public void createCalendarEvent(String parameters) {
        if (parameters != null && !parameters.equals("")) {

            try {
                JSONObject params = new JSONObject(parameters);
                CalendarEventWrapper calendarEventWrapper = new CalendarEventWrapper(params);
                ManagersResolver.getInstance().getDeviceManager().createCalendarEvent(calendarEventWrapper);
            }
            catch (Exception e) {
                mJsi.onError("create_calendar_event_error", JSInterface.ACTION_CREATE_CALENDAR_EVENT);
            }
        }
    }
}