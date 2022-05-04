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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wraps an JSON data element.
 * Extended ISO8601 standart.
 * See more at http://dev.w3.org/html5/spec/single-page.html (2.5.5 Dates and times)
 */
public class DateWrapper {

    private static final String DATE_TIME_PATTERN_SEP1 = "T";
    private static final String DATE_TIME_PATTERN_SEP2 = " ";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN1 = "HH:mm'Z'";
    private static final String TIME_PATTERN2 = "HH:mm:ss.S";
    private static final String TIME_PATTERN3 = "HH:mm:ss.SS";
    private static final String TIME_PATTERN4 = "HH:mm:ss.SSS";
    private static final String TIME_PATTERN5 = "HH:mm:ss.SZZZ";
    private static final String TIME_PATTERN6 = "HH:mm:ss.SSZZZ";
    private static final String TIME_PATTERN7 = "HH:mm:ss.SSSZZZ";
    private static final String TIME_PATTERN8 = "HH:mm:ssZZZ";
    private static final String TIME_PATTERN9 = "HH:mmZZZ";

    private Date date;
    private String timeZone;
    private boolean isEmpty;

    public boolean isEmpty() {
        return isEmpty;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        if (timeZone != null && !timeZone.startsWith("GMT")) {
            timeZone = "GMT" + timeZone;
        }
        this.timeZone = timeZone;
    }

    public long getTime() {
        return date != null ? date.getTime() : 0;
    }

    private static SimpleDateFormat tryPattern(String datetime, String pattern) {
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(pattern);
            dateTimeFormat.parse(datetime);
            return dateTimeFormat;
        }
        catch (java.text.ParseException pe) {
            return null;
        }
    }

    public DateWrapper(String dateTimeString) throws java.text.ParseException {
        if (dateTimeString != null) {
            String date = null;
            String time = null;
            String sep = null;

            if (dateTimeString.contains(DATE_TIME_PATTERN_SEP1)) {
                date = dateTimeString.substring(0, dateTimeString.indexOf(DATE_TIME_PATTERN_SEP1));
                time = dateTimeString.substring(dateTimeString.indexOf(DATE_TIME_PATTERN_SEP1) + 1);
                sep = "'" + DATE_TIME_PATTERN_SEP1 + "'";
            }
            else if (dateTimeString.contains(DATE_TIME_PATTERN_SEP2)) {
                date = dateTimeString.substring(0, dateTimeString.indexOf(DATE_TIME_PATTERN_SEP2));
                time = dateTimeString.substring(dateTimeString.indexOf(DATE_TIME_PATTERN_SEP2) + 1);
                sep = "'" + DATE_TIME_PATTERN_SEP2 + "'";
            }
            else {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_PATTERN);
                this.date = dateTimeFormat.parse(dateTimeString);
            }

            if (date != null && time != null && sep != null) {
                getDate(dateTimeString, time, sep);
            }
        }
        else {
            isEmpty = true;
        }
    }

    private void getDate(String dateTimeString, String time, String sep) throws ParseException {
        String dateTimePattern = null;
        boolean hasTimeZone = false;

        if (tryPattern(time, TIME_PATTERN1) != null) {
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN1;
        }
        else if (tryPattern(time, TIME_PATTERN2) != null) {
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN2;
        }
        else if (tryPattern(time, TIME_PATTERN3) != null) {
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN3;
        }
        else if (tryPattern(time, TIME_PATTERN4) != null) {
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN4;
        }
        else if (tryPattern(time, TIME_PATTERN5) != null) {
            hasTimeZone = true;
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN5;
        }
        else if (tryPattern(time, TIME_PATTERN6) != null) {
            hasTimeZone = true;
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN6;
        }
        else if (tryPattern(time, TIME_PATTERN7) != null) {
            hasTimeZone = true;
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN7;
        }
        else if (tryPattern(time, TIME_PATTERN8) != null) {
            hasTimeZone = true;
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN8;
        }
        else if (tryPattern(time, TIME_PATTERN9) != null) {
            hasTimeZone = true;
            dateTimePattern = DATE_PATTERN + sep + TIME_PATTERN9;
        }

        if (dateTimePattern != null) {
            if (hasTimeZone) {
                String timeZone = dateTimeString.substring(dateTimeString.length() - 6); // +00:00
                setTimeZone(timeZone);
            }

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateTimePattern);
            date = dateTimeFormat.parse(dateTimeString);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateWrapper dateWrapper = (DateWrapper) o;

        return date != null ? date.equals(dateWrapper.date) : dateWrapper.date == null;
    }

    @Override
    public int hashCode() {
        return date != null ? date.hashCode() : 0;
    }
}
