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

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;

final class CalendarGTE14 implements ICalendar
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

		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType("vnd.android.cursor.item/event");

		intent.putExtra(CalendarContract.Events.TITLE, summary);
		intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
		intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStart() != null ? event.getStart().getTime() : System.currentTimeMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEnd() != null ? event.getEnd().getTime() : System.currentTimeMillis() + 1800 * 1000);
		intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
		intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_DEFAULT);
		intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);

        CalendarRepeatRule rules = event.getRecurrence();
		if (rules != null)
		{
			String rule = createRule(rules);

			intent.putExtra(CalendarContract.Events.RRULE, rule);
		}

		// Has alarm: 0~ false; 1~ true
		if (event.getReminder() != null && !event.getReminder().isEmpty())
		{
			intent.putExtra(CalendarContract.Events.HAS_ALARM, true);
		}

		ExternalViewerUtils.startActivity(context, intent);
	}

    private String createRule(CalendarRepeatRule rules)
	{
		StringBuilder rule = setFrequencyRule(rules);

		if (rules.getDaysInWeek() != null && rules.getDaysInWeek().length > 0)
		{
			setDayRule(rules, rule);
		}

		rule.append(produceRuleValuesStringForKey(rules.getDaysInMonth(), "BYMONTHDAY"));
		rule.append(produceRuleValuesStringForKey(rules.getDaysInYear(), "BYYEARDAY"));
		rule.append(produceRuleValuesStringForKey(rules.getMonthsInYear(), "BYMONTH"));
		rule.append(produceRuleValuesStringForKey(rules.getWeeksInMonth(), "BYWEEKNO"));

		if (rules.getExpires() != null)
		{
            rule.append(";UNTIL=")
                .append(rules.getExpires().getTime());
		}

		return rule.toString();

	}

    private void setDayRule(CalendarRepeatRule rules, StringBuilder rule)
	{
		StringBuilder days = new StringBuilder();
		for (Short day : rules.getDaysInWeek())
		{
			if (day != null)
			{
				switch (day)
				{
					case 0:
						days.append(",SU");
						break;
					case 1:
						days.append(",MO");
						break;
					case 2:
						days.append(",TU");
						break;
					case 3:
						days.append(",WE");
						break;
					case 4:
						days.append(",TH");
						break;
					case 5:
						days.append(",FR");
						break;
					case 6:
						days.append(",SA");
						break;
				}
			}
		}

		if (days.length() > 0)
		{
			days = days.deleteCharAt(0);
            rule.append(";BYDAY=")
                .append(days.toString());
		}
	}

    private StringBuilder setFrequencyRule(CalendarRepeatRule rules)
	{
		StringBuilder rule = new StringBuilder();
		
		switch (rules.getFrequency())
		{
			case DAILY:
				rule.append("FREQ=DAILY");
				break;
			case MONTHLY:
				rule.append("FREQ=MONTHLY");
				break;
			case WEEKLY:
				rule.append("FREQ=WEEKLY");
				break;
			case YEARLY:
				rule.append("FREQ=YEARLY");
				break;
			default:
				break;
		}

		if (rules.getInterval() != null)
		{
            rule.append(";INTERVAL=")
                .append(rules.getInterval());
		}
		return rule;
	}

	private String produceRuleValuesStringForKey(Short[] values, String key)
	{
		if (values != null && values.length > 0)
		{
			StringBuilder cValues = new StringBuilder();
			for (Short value : values)
			{
				if (value != null)
				{
                    cValues.append(",")
                           .append(value);
				}
			}

			if (cValues.length() > 0)
			{
				cValues = cValues.deleteCharAt(0);
				return ";" + key + "=" + cValues.toString();
			}

			return "";
		}
		else
		{
			return "";
		}
	}
}