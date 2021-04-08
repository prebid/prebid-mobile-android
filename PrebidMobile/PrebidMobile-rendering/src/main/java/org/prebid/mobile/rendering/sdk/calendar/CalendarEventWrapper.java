package org.prebid.mobile.rendering.sdk.calendar;

import org.json.JSONObject;
import org.prebid.mobile.rendering.utils.logger.OXLog;

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

	public enum Transparency
	{
		TRANSPARENT,
		OPAQUE,
		UNKNOWN
	}

	private String mId;
	private String mDescription;
	private String mLocation;
	private String mSummary;
    private DateWrapper mStart;
    private DateWrapper mEnd;
	private Status mStatus;
	private Transparency mTransparency;
    private CalendarRepeatRule mRecurrence;
    private DateWrapper mReminder;

	public String getId()
	{
		return mId;
	}

	public void setId(String id)
	{
        mId = id;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public void setDescription(String description)
	{
        mDescription = description;
	}

	public String getLocation()
	{
		return mLocation;
	}

	public void setLocation(String location)
	{
        mLocation = location;
	}

	public String getSummary()
	{
		return mSummary;
	}

	public void setSummary(String summary)
	{
        mSummary = summary;
	}

    public DateWrapper getStart()
	{
		return mStart;
	}

	public void setStart(String start)
	{
		try
		{
            mStart = new DateWrapper(start);
		}
		catch (ParseException e)
		{
            OXLog.error(TAG, "Failed to parse start date:" + e.getMessage());
		}
	}

    public DateWrapper getEnd()
	{
		return mEnd;
	}

	public void setEnd(String end)
	{
		try
		{
            mEnd = new DateWrapper(end);
		}
		catch (ParseException e)
		{
            OXLog.error(TAG, "Failed to parse end date:" + e.getMessage());
		}
	}

	public Status getStatus()
	{
		return mStatus;
	}

	public void setStatus(Status status)
	{
        mStatus = status;
	}

	public Transparency getTransparency()
	{
		return mTransparency;
	}

	public void setTransparency(Transparency transparency)
	{
        mTransparency = transparency;
	}

    public CalendarRepeatRule getRecurrence()
	{
		return mRecurrence;
	}

    public void setRecurrence(CalendarRepeatRule recurrence)
	{
        mRecurrence = recurrence;
	}

    public DateWrapper getReminder()
	{
		return mReminder;
	}

	public void setReminder(String reminder)
	{
		try
		{
            mReminder = new DateWrapper(reminder);
		}
		catch (ParseException e)
		{
            OXLog.error(TAG, "Failed to parse reminder date:" + e.getMessage());
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
                OXLog.error(TAG, "Failed to set calendar recurrence:" + e.getMessage());
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