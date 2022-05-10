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
import org.prebid.mobile.LogUtil;

import java.text.ParseException;


/**
 * Wraps an JSON calendar event element.
 */
public final class CalendarEventWrapper
{
    public final static String TAG = CalendarEventWrapper.class.getSimpleName();
	
	public enum Status
	{
		PENDING,
		TENTATIVE,
		CONFIRMED,
		CANCELLED,
		UNKNOWN
	}

	public enum Transparency {
		TRANSPARENT,
		OPAQUE,
		UNKNOWN
	}

	private String id;
	private String description;
	private String location;
	private String summary;
	private DateWrapper start;
	private DateWrapper end;
	private Status status;
	private Transparency transparency;
	private CalendarRepeatRule recurrence;
	private DateWrapper reminder;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getSummary()
	{
		return summary;
	}

	public void setSummary(String summary)
	{
		this.summary = summary;
	}

    public DateWrapper getStart()
	{
		return start;
	}

	public void setStart(String start)
	{
		try
		{
			this.start = new DateWrapper(start);
		}
		catch (ParseException e)
		{
            LogUtil.error(TAG, "Failed to parse start date:" + e.getMessage());
		}
	}

    public DateWrapper getEnd()
	{
		return end;
	}

	public void setEnd(String end)
	{
		try
		{
			this.end = new DateWrapper(end);
		}
		catch (ParseException e)
		{
			LogUtil.error(TAG, "Failed to parse end date:" + e.getMessage());
		}
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public Transparency getTransparency()
	{
		return transparency;
	}

	public void setTransparency(Transparency transparency)
	{
		this.transparency = transparency;
	}

    public CalendarRepeatRule getRecurrence()
	{
		return recurrence;
	}

    public void setRecurrence(CalendarRepeatRule recurrence)
	{
		this.recurrence = recurrence;
	}

    public DateWrapper getReminder()
	{
		return reminder;
	}

	public void setReminder(String reminder)
	{
		try
		{
			this.reminder = new DateWrapper(reminder);
		}
		catch (ParseException e)
		{
			LogUtil.error(TAG, "Failed to parse reminder date:" + e.getMessage());
		}
	}

    public CalendarEventWrapper(JSONObject params)
	{
		setId(params.optString("id", null));
		setDescription(params.optString("description", null));
		setLocation(params.optString("location", null));
		setSummary(params.optString("summary", null));
		setStart(params.optString("start", null));
		setEnd(params.optString("end", null));

		String status = params.optString("status", null);
		setCalendarStatus(status);

		String transparency = params.optString("transparency", null);
		setCalendarTransparency(transparency);

		String recurrence = params.optString("recurrence", null);
		setCalendarRecurrence(recurrence);

		setReminder(params.optString("reminder", null));
	}

	private void setCalendarRecurrence(String recurrence)
	{
		if (recurrence != null && !recurrence.equals(""))
		{
			try
			{
				JSONObject obj = new JSONObject(recurrence);
                setRecurrence(new CalendarRepeatRule(obj));
			}
			catch (Exception e)
			{
				LogUtil.error(TAG, "Failed to set calendar recurrence:" + e.getMessage());
			}
		}
	}

	private void setCalendarTransparency(String transparency)
	{
		if (transparency != null && !transparency.equals(""))
		{
			if (transparency.equalsIgnoreCase("transparent"))
			{
				setTransparency(Transparency.TRANSPARENT);
			}
			else if (transparency.equalsIgnoreCase("opaque"))
			{
				setTransparency(Transparency.OPAQUE);
			}
			else
			{
				setTransparency(Transparency.UNKNOWN);
			}
		}
		else
		{
			setTransparency(Transparency.UNKNOWN);
		}
	}

	private void setCalendarStatus(String status)
	{
		if (status != null && !status.equals(""))
		{
			if (status.equalsIgnoreCase("pending"))
			{
				setStatus(Status.PENDING);
			}
			else if (status.equalsIgnoreCase("tentative"))
			{
				setStatus(Status.TENTATIVE);
			}
			else if (status.equalsIgnoreCase("confirmed"))
			{
				setStatus(Status.CONFIRMED);
			}
			else if (status.equalsIgnoreCase("cancelled"))
			{
				setStatus(Status.CANCELLED);
			}
			else
			{
				setStatus(Status.UNKNOWN);
			}
		}
		else
		{
			setStatus(Status.UNKNOWN);
		}
	}

}