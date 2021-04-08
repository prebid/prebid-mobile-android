package org.prebid.mobile.rendering.mraid.methods;

import org.json.JSONObject;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.calendar.CalendarEventWrapper;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;

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