package org.prebid.mobile.rendering.sdk.calendar;

import android.content.Context;
import android.content.Intent;

final class CalendarLT14 implements ICalendar
{
	@Override
    public void createCalendarEvent(Context context, CalendarEventWrapper event)
	{
		String summary = event.getSummary();
		String description = event.getDescription();
		String location = event.getLocation();

		if (summary == null)
		{
			summary = "";
		}

		if (description == null)
		{
			description = "";
		}

		if (location == null)
		{
			location = "";
		}

		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("title", summary);
		intent.putExtra("description", description);
		intent.putExtra("eventLocation", location);
		intent.putExtra("beginTime", event.getStart() != null ? event.getStart().getTime() : System.currentTimeMillis());
		intent.putExtra("endTime", event.getEnd() != null ? event.getEnd().getTime() : System.currentTimeMillis() + 1800 * 1000);
		intent.putExtra("allDay", false);

		// Visibility: 0~ default; 1~ confidential; 2~ private; 3~ public
		intent.putExtra("visibility", 0);

		// Has alarm: 0~ false; 1~ true
		if (event.getReminder() != null && !event.getReminder().isEmpty())
		{
			intent.putExtra("hasAlarm", 1);
		}

		context.startActivity(intent);

	}
}