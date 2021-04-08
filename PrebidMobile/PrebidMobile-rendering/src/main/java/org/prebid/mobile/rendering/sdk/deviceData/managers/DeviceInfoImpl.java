package org.prebid.mobile.rendering.sdk.deviceData.managers;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.sdk.BaseManager;
import org.prebid.mobile.rendering.sdk.calendar.CalendarEventWrapper;
import org.prebid.mobile.rendering.sdk.calendar.CalendarFactory;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

public class DeviceInfoImpl extends BaseManager implements DeviceInfoManager {

    private String TAG = DeviceInfoImpl.class.getSimpleName();
    private TelephonyManager mTelephonyManager;
    private WindowManager mWindowManager;
    private PowerManager mPowerManager;
    private KeyguardManager mKeyguardManager;
    private PackageManager mPackageManager;

    /**
     * @see DeviceInfoManager
     */
    @Override
    public void init(Context context) {
        super.init(context);
        if (super.isInit() && getContext() != null) {
            mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            mPowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            mKeyguardManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
            mPackageManager = getContext().getPackageManager();

            hasTelephony();
        }
    }

    @Override
    public boolean hasTelephony() {
        if (mTelephonyManager == null) {
            return false;
        }

        if (mPackageManager == null) {
            return false;
        }

        return mPackageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public String getMccMnc() {
        String operatorISO;
        if (isInit() && mTelephonyManager != null) {
            //This API does not need permission check for PHONE_STATE.
            operatorISO = mTelephonyManager.getNetworkOperator();

            String MCC;
            String MNC;
            if (operatorISO != null && !operatorISO.equals("") && operatorISO.length() > 3) {
                MNC = operatorISO.substring(0, 3);
                MCC = operatorISO.substring(3);
                return MNC + '-' + MCC;
            }
        }
        return null;
    }

    @Override
    public String getCarrier() {
        String networkOperatorName = null;
        if (isInit() && mTelephonyManager != null) {
            //This API does not need permission check for PHONE_STATE.
            networkOperatorName = mTelephonyManager.getNetworkOperatorName();
        }
        return networkOperatorName;
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public boolean isPermissionGranted(String permission) {
        boolean isPermissionGranted = false;
        if (isInit() && getContext() != null) {
            isPermissionGranted = isInit() && getContext().checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return isPermissionGranted;
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public int getDeviceOrientation() {
        int deviceOrientation = Configuration.ORIENTATION_UNDEFINED;
        if (isInit() && getContext() != null) {
            Configuration config = isInit() ? getContext().getResources().getConfiguration() : null;
            deviceOrientation = config != null
                                ? config.orientation
                                : Configuration.ORIENTATION_UNDEFINED;
        }
        return deviceOrientation;
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public void dispose() {
        super.dispose();

        mTelephonyManager = null;
        mKeyguardManager = null;
        mPowerManager = null;
        mWindowManager = null;
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public int getScreenWidth() {
        return Utils.getScreenWidth(mWindowManager);
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public int getScreenHeight() {
        return Utils.getScreenHeight(mWindowManager);
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public boolean isScreenOn() {
        if (mPowerManager != null) {
            return mPowerManager.isScreenOn();
        }
        return false;
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public boolean isScreenLocked() {
        if (mKeyguardManager != null) {
            return mKeyguardManager.inKeyguardRestrictedInputMode();
        }
        return false;
    }

    @Override
    public boolean isActivityOrientationLocked(Context context) {
        if (!(context instanceof Activity)) {
            OXLog.debug(TAG, "isScreenOrientationLocked() executed with non-activity context. Returning false.");
            return false;
        }

        int requestedOrientation = ((Activity) context).getRequestedOrientation();
        return requestedOrientation == SCREEN_ORIENTATION_PORTRAIT
               || requestedOrientation == SCREEN_ORIENTATION_REVERSE_PORTRAIT
               || requestedOrientation == SCREEN_ORIENTATION_LANDSCAPE
               || requestedOrientation == SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public void createCalendarEvent(final CalendarEventWrapper event) {
        if (event != null && getContext() != null) {
            CalendarFactory.getCalendarInstance().createCalendarEvent(getContext(), event);
        }
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public void storePicture(String url) throws Exception {
        if (!Utils.isExternalStorageAvailable()) {
            OXLog.error(TAG, "storePicture: Failed. External storage is not available");
            return;
        }
        String fileName = Utils.md5(url);
        final String fileExtension = Utils.getFileExtension(url);

        if (!TextUtils.isEmpty(fileExtension)) {
            fileName = fileName + fileExtension;
        }

        OutputStream outputStream = getOutputStream(fileName);

        if (outputStream == null) {
            OXLog.error(TAG, "Could not get Outputstream to write file to");
            return;
        }

        URL wrappedUrl = new URL(url);
        /* Open a connection to that URL. */
        URLConnection urlConnection = wrappedUrl.openConnection();
        writeToFile(outputStream, urlConnection.getInputStream());
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public void playVideo(String url) {
        if (getContext() != null) {
            final Intent startBrowserActivity = new Intent(getContext(), AdBrowserActivity.class);
            startBrowserActivity.putExtra(AdBrowserActivity.EXTRA_IS_VIDEO, true);
            startBrowserActivity.putExtra(AdBrowserActivity.EXTRA_URL, url);
            if (ExternalViewerUtils.isActivityCallable(getContext(), startBrowserActivity)) {
                getContext().startActivity(startBrowserActivity);
            }
            else {
                ExternalViewerUtils.startExternalVideoPlayer(getContext(), url);
            }
        }
    }

    /**
     * @see DeviceInfoManager
     */
    @Override
    public boolean canStorePicture() {
        return true;
    }

    @Override
    public float getDeviceDensity() {
        if (getContext() != null) {
            return getContext().getResources().getDisplayMetrics().density;
        }

        return 1.0f;
    }

    @Override
    public boolean hasGps() {
        return mPackageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    @VisibleForTesting
    OutputStream getOutputStream(String fileName) throws FileNotFoundException {
        if (Utils.atLeastQ()) {
            return getOutPutStreamForQ(fileName, getContext());
        }
        else {
            return getOutputStreamPreQ(fileName);
        }
    }

    @VisibleForTesting
    OutputStream getOutputStreamPreQ(String fileName) throws FileNotFoundException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        path.mkdirs();
        return new FileOutputStream(new File(path, fileName));
    }

    @VisibleForTesting
    OutputStream getOutPutStreamForQ(String filename, Context context)
    throws FileNotFoundException {
        if (context == null) {
            OXLog.debug(TAG, "getOutPutStreamForQ: Failed. Context is null");
            return null;
        }

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        Uri contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        Uri insert = contentResolver.insert(contentUri, contentValues);
        if (insert == null) {
            OXLog.debug(TAG, "Could not save content uri");
            return null;
        }
        return contentResolver.openOutputStream(insert);
    }

    private void writeToFile(OutputStream outputStream, InputStream inputStream) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //We create an array of bytes
        byte[] data = new byte[1024];
        int current;
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        /*
         * Read bytes to the Buffer until there is nothing more to read(-1).
         */
        try {
            while ((current = bufferedInputStream.read(data, 0, data.length)) != -1) {
                byteArrayOutputStream.write(data, 0, current);
            }

            /* Convert the Bytes read to a String. */
            bufferedOutputStream.write(byteArrayOutputStream.toByteArray());
        }
        finally {
            byteArrayOutputStream.close();
            bufferedInputStream.close();
            bufferedOutputStream.close();
        }
    }
}
