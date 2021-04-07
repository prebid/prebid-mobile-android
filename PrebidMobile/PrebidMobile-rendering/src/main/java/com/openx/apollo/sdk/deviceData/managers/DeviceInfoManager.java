package com.openx.apollo.sdk.deviceData.managers;

import android.content.Context;

import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.sdk.calendar.CalendarEventWrapper;

import java.io.IOException;

/**
 * Manager for retrieving device information.
 *
 * @see ManagersResolver
 */
public interface DeviceInfoManager {
    /**
     * Get the mcc-mnc
     *
     * @return the mcc-mnc
     */
    String getMccMnc();

    /**
     * Get the device carrier
     *
     * @return the carrier
     */
    String getCarrier();

    /**
     * Check if is permission granted.
     *
     * @param permission the permission name
     * @return true, if is permission granted
     */
    boolean isPermissionGranted(String permission);

    /**
     * Get the device orientation. Return values can be compared to
     * android.content.res.Configuration orientation values
     *
     * @return the device orientation
     */
    int getDeviceOrientation();

    /**
     * Get the screen width.
     *
     * @return the screen width
     */
    int getScreenWidth();

    /**
     * Get the screen height.
     *
     * @return the screen height
     */
    int getScreenHeight();

    /**
     * Get device screen state.
     *
     * @return true if screen is on
     */
    boolean isScreenOn();

    /**
     * Get device screen lock state.
     *
     * @return true if screen is locked
     */
    boolean isScreenLocked();

    /**
     * @param context activity context.
     * @return true if activity is locked in portrait || landscape (including reverse portrait and reverse landscape)
     */
    boolean isActivityOrientationLocked(Context context);

    /**
     * Allow to create new calendar event.
     *
     * @param event is calendar event filled object
     */
    void createCalendarEvent(CalendarEventWrapper event);

    /**
     * Allow to store picture on device.
     *
     * @param url network URL to the picture
     * @throws Exception if there is
     */
    void storePicture(String url) throws Exception;

    /**
     * Allow to play video inside internal player
     *
     * @param url network URL to the video
     * @throws IOException if there is
     */
    void playVideo(String url);

    /**
     * Check the state to have ability to save picture on device.
     *
     * @return true if can process with picture saving
     */
    boolean canStorePicture();

    /**
     * Check the state that device has telephony to do calls/sms
     *
     * @return true if has telephony
     */
    boolean hasTelephony();

    /**
     * Check the device screen density
     *
     * @return float device density
     */
    float getDeviceDensity();

    /**
     * Checks if the device can use location features
     *
     * @return true if location feature is available
     */
    boolean hasGps();
}
