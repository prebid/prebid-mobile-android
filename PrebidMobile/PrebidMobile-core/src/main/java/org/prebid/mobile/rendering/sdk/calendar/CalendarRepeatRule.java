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

import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;

import java.text.ParseException;

/**
 * Wraps an JSON calendar repeat rule element.
 */
public final class CalendarRepeatRule {

    private final static String TAG = CalendarRepeatRule.class.getSimpleName();

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
        UNKNOWN
    }

    private Frequency frequency;
    private Integer interval = 1;
    private DateWrapper expires;
    private DateWrapper[] exceptionDates;
    private Short[] daysInWeek;
    private Short[] daysInMonth;
    private Short[] daysInYear;
    private Short[] weeksInMonth;
    private Short[] monthsInYear;

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public DateWrapper getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        try {
            this.expires = new DateWrapper(expires);
        }
        catch (ParseException e) {
            LogUtil.error(TAG, "Failed to parse expires date:" + e.getMessage());
        }
    }

    public DateWrapper[] getExceptionDates() {
        return exceptionDates;
    }

    public void setExceptionDates(String[] exceptionDates) {
        if (exceptionDates != null) {
            this.exceptionDates = new DateWrapper[exceptionDates.length];
            int ind = 0;
            for (String dateTimeString : exceptionDates) {
                try {
                    this.exceptionDates[ind] = new DateWrapper(dateTimeString);
                }
                catch (ParseException e) {
                    // Date can't be parsed
                    this.exceptionDates[ind] = null;

                    LogUtil.error(TAG, "Failed to parse exception date:" + e.getMessage());
                }
                ++ind;
            }
        }
    }

    public Short[] getDaysInWeek() {
        return daysInWeek;
    }

    public void setDaysInWeek(Short[] daysInWeek) {
        this.daysInWeek = daysInWeek;
    }

    public Short[] getDaysInMonth() {
        return daysInMonth;
    }

    public void setDaysInMonth(Short[] daysInMonth) {
        this.daysInMonth = daysInMonth;
    }

    public Short[] getDaysInYear() {
        return daysInYear;
    }

    public void setDaysInYear(Short[] daysInYear) {
        this.daysInYear = daysInYear;
    }

    public Short[] getWeeksInMonth() {
        return weeksInMonth;
    }

    public void setWeeksInMonth(Short[] weeksInMonth) {
        this.weeksInMonth = weeksInMonth;
    }

    public Short[] getMonthsInYear() {
        return monthsInYear;
    }

    public void setMonthsInYear(Short[] monthsInYear) {
        this.monthsInYear = monthsInYear;
    }

    public CalendarRepeatRule(JSONObject params) {
        String frequency = params.optString("frequency", null);
        setFrequency(frequency);

        // Default 1, may be null
        String interval = params.optString("interval", null);
        setInterval(interval);

        String expires = params.optString("expires", null);
        if (expires != null && !expires.equals("")) {
            setExpires(expires);
        }

        JSONArray exceptionDates = params.optJSONArray("exceptionDates");
        setExceptionDates(exceptionDates);

        JSONArray daysInWeek = params.optJSONArray("daysInWeek");
        setDaysInWeek(daysInWeek);

        JSONArray daysInMonth = params.optJSONArray("daysInMonth");
        setDaysInMonth(daysInMonth);

        JSONArray daysInYear = params.optJSONArray("daysInYear");
        setDaysInYear(daysInYear);

        JSONArray weeksInMonth = params.optJSONArray("weeksInMonth");
        setWeeksInMonth(weeksInMonth);

        JSONArray monthsInYear = params.optJSONArray("monthsInYear");
        setMonthsInYear(monthsInYear);
    }

    private void setMonthsInYear(JSONArray monthsInYear) {
        if (monthsInYear != null) {
            try {
                String entry;
                Short month;
                Short[] months = new Short[monthsInYear.length()];
                for (int i = 0; i < monthsInYear.length(); ++i) {
                    entry = monthsInYear.optString(i, null);
                    month = entry != null && !entry.equals("") ? Short.valueOf(entry) : null;
                    months[i] = month;
                }
                setMonthsInYear(months);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set months in year:" + e.getMessage());
            }
        }
    }

    private void setWeeksInMonth(JSONArray weeksInMonth) {
        if (weeksInMonth != null) {
            try {
                String entry;
                Short week;
                Short[] weeks = new Short[weeksInMonth.length()];
                for (int i = 0; i < weeksInMonth.length(); ++i) {
                    entry = weeksInMonth.optString(i, null);
                    week = entry != null && !entry.equals("") ? Short.valueOf(entry) : null;
                    weeks[i] = week;
                }
                setWeeksInMonth(weeks);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set weeks in month:" + e.getMessage());
            }
        }
    }

    private void setDaysInYear(JSONArray daysInYear) {
        if (daysInYear != null) {
            try {
                String entry;
                Short day;
                Short[] days = new Short[daysInYear.length()];
                for (int i = 0; i < daysInYear.length(); ++i) {
                    entry = daysInYear.optString(i, null);
                    day = entry != null && !entry.equals("") ? Short.valueOf(entry) : null;
                    days[i] = day;
                }
                setDaysInYear(days);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set days in year:" + e.getMessage());
            }
        }
    }

    private void setDaysInMonth(JSONArray daysInMonth) {
        if (daysInMonth != null) {
            try {
                String entry;
                Short day;
                Short[] days = new Short[daysInMonth.length()];
                for (int i = 0; i < daysInMonth.length(); ++i) {
                    entry = daysInMonth.optString(i, null);
                    day = entry != null && !entry.equals("") ? Short.valueOf(entry) : null;
                    days[i] = day;
                }
                setDaysInMonth(days);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set days in month:" + e.getMessage());
            }
        }
    }

    private void setDaysInWeek(JSONArray daysInWeek) {
        if (daysInWeek != null) {
            try {
                String entry;
                Short day;
                Short[] days = new Short[daysInWeek.length()];
                for (int i = 0; i < daysInWeek.length(); ++i) {
                    entry = daysInWeek.optString(i, null);
                    day = entry != null && !entry.equals("") ? Short.valueOf(entry) : null;
                    days[i] = day;
                }
                setDaysInWeek(days);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set days in week:" + e.getMessage());
            }
        }
    }

    private void setExceptionDates(JSONArray exceptionDates) {
        if (exceptionDates != null) {
            try {
                String date;
                String[] dates = new String[exceptionDates.length()];
                for (int i = 0; i < exceptionDates.length(); ++i) {
                    date = exceptionDates.optString(i, null);
                    dates[i] = date;
                }
                setExceptionDates(dates);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set exception days:" + e.getMessage());
            }
        }
    }

    private void setInterval(String interval) {
        if (interval != null && !interval.equals("")) {
            try {
                setInterval(Integer.parseInt(interval));
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to set interval:" + e.getMessage());
            }
        }
    }

    private void setFrequency(String frequency) {
        if (frequency != null && !frequency.equals("")) {
            if (frequency.equalsIgnoreCase("daily")) {
                setFrequency(Frequency.DAILY);
            }
            else if (frequency.equalsIgnoreCase("monthly")) {
                setFrequency(Frequency.MONTHLY);
            }
            else if (frequency.equalsIgnoreCase("weekly")) {
                setFrequency(Frequency.WEEKLY);
            }
            else if (frequency.equalsIgnoreCase("yearly")) {
                setFrequency(Frequency.YEARLY);
            }
            else {
                setFrequency(Frequency.UNKNOWN);
            }
        }
        else {
            setFrequency(Frequency.UNKNOWN);
        }
    }
}