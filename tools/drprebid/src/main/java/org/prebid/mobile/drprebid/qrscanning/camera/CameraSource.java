package org.prebid.mobile.drprebid.qrscanning.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.StringDef;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraSource {
    public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private static final String TAG = "OpenCameraSource";

    /**
     * The dummy surface texture must be assigned a chosen name.  Since we never use an OpenGL
     * context, we can choose any ID we want here.
     */
    private static final int DUMMY_TEXTURE_NAME = 100;

    /**
     * If the absolute difference between a preview size aspect ratio and a picture size aspect
     * ratio is less than this tolerance, they are considered to be the same aspect ratio.
     */
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    @StringDef({
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_AUTO,
            Camera.Parameters.FOCUS_MODE_EDOF,
            Camera.Parameters.FOCUS_MODE_FIXED,
            Camera.Parameters.FOCUS_MODE_INFINITY,
            Camera.Parameters.FOCUS_MODE_MACRO
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FocusMode {
    }

    @StringDef({
            Camera.Parameters.FLASH_MODE_ON,
            Camera.Parameters.FLASH_MODE_OFF,
            Camera.Parameters.FLASH_MODE_AUTO,
            Camera.Parameters.FLASH_MODE_RED_EYE, Camera.Parameters.FLASH_MODE_TORCH})
    @Retention(RetentionPolicy.SOURCE)
    private @interface FlashMode {}

    private Context context;

    private final Object cameraLock = new Object();

    // Guarded by cameraLock
    private Camera camera;

    private int facing = CAMERA_FACING_BACK;

    /**
     * Rotation of the device, and thus the associated preview images captured from the device.
     * See {@link Frame.Metadata#getRotation()}.
     */
    private int rotation;

    private Size previewSize;

    // These values may be requested by the caller.  Due to hardware limitations, we may need to
    // select close, but not exactly the same values for these.
    private float requestedFps = 30.0f;
    private int requestedPreviewWidth = 1024;
    private int requestedPreviewHeight = 768;


    private String focusMode = null;
    private String flashMode = null;

    // These instances need to be held onto to avoid GC of their underlying resources.  Even though
    // these aren't used outside of the method that creates them, they still must have hard
    // references maintained to them.
    private SurfaceView dummySurfaceView;
    private SurfaceTexture dummySurfaceTexture;

    /**
     * Dedicated thread and associated runnable for calling into the detector with frames, as the
     * frames become available from the camera.
     */
    private Thread processingThread;
    private FrameProcessingRunnable frameProcessor;

    /**
     * Map to convert between a byte array, received from the camera, and its associated byte
     * buffer.  We use byte buffers internally because this is a more efficient way to call into
     * native code later (avoids a potential copy).
     */
    private Map<byte[], ByteBuffer> bytesToByteBuffer = new HashMap<>();

    //==============================================================================================
    // Builder
    //==============================================================================================

    /**
     * Builder for configuring and creating an associated camera source.
     */
    public static class Builder {

        private final Detector<?> detector;
        private CameraSource cameraSource = new CameraSource();

        /**
         * Creates a camera source builder with the supplied context and detector.  Camera preview
         * images will be streamed to the associated detector upon starting the camera source.
         */
        public Builder(
                Context context,
                Detector<?> detector
        ) {
            if (context == null) {
                throw new IllegalArgumentException("No context supplied.");
            }
            if (detector == null) {
                throw new IllegalArgumentException("No detector supplied.");
            }

            this.detector = detector;
            cameraSource.context = context;
        }

        /**
         * Sets the requested frame rate in frames per second.  If the exact requested value is not
         * not available, the best matching available value is selected.   Default: 30.
         */
        public Builder setRequestedFps(float fps) {
            if (fps <= 0) {
                throw new IllegalArgumentException("Invalid fps: " + fps);
            }
            cameraSource.requestedFps = fps;
            return this;
        }

        public Builder setFocusMode(@FocusMode String mode) {
            cameraSource.focusMode = mode;
            return this;
        }

        public Builder setFlashMode(@FlashMode String mode) {
            cameraSource.flashMode = mode;
            return this;
        }

        /**
         * Sets the desired width and height of the camera frames in pixels.  If the exact desired
         * values are not available options, the best matching available options are selected.
         * Also, we try to select a preview size which corresponds to the aspect ratio of an
         * associated full picture size, if applicable.  Default: 1024x768.
         */
        public Builder setRequestedPreviewSize(int width, int height) {
            // Restrict the requested range to something within the realm of possibility.  The
            // choice of 1000000 is a bit arbitrary -- intended to be well beyond resolutions that
            // devices can support.  We bound this to avoid int overflow in the code later.
            final int MAX = 1000000;
            if ((width <= 0) || (width > MAX) || (height <= 0) || (height > MAX)) {
                throw new IllegalArgumentException("Invalid preview size: " + width + "x" + height);
            }
            cameraSource.requestedPreviewWidth = width;
            cameraSource.requestedPreviewHeight = height;
            return this;
        }

        /**
         * Sets the camera to use (either {@link #CAMERA_FACING_BACK} or
         * {@link #CAMERA_FACING_FRONT}). Default: back facing.
         */
        public Builder setFacing(int facing) {
            if ((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT)) {
                throw new IllegalArgumentException("Invalid camera: " + facing);
            }
            cameraSource.facing = facing;
            return this;
        }

        /**
         * Creates an instance of the camera source.
         */
        public CameraSource build() {
            cameraSource.frameProcessor = cameraSource.new FrameProcessingRunnable(detector);
            return cameraSource;
        }
    }

    //==============================================================================================
    // Bridge Functionality for the Camera1 API
    //==============================================================================================

    /**
     * Callback interface used to signal the moment of actual image capture.
     */
    public interface ShutterCallback {
        /**
         * Called as near as possible to the moment when a photo is captured from the sensor. This
         * is a good opportunity to play a shutter sound or give other feedback of camera operation.
         * This may be some time after the photo was triggered, but some time before the actual data
         * is available.
         */
        void onShutter();
    }

    /**
     * Callback interface used to supply image data from a photo capture.
     */
    public interface PictureCallback {
        /**
         * Called when image data is available after a picture is taken.  The format of the data
         * is a jpeg binary.
         */
        void onPictureTaken(byte[] data);
    }

    /**
     * Callback interface used to notify on completion of camera auto focus.
     */
    public interface AutoFocusCallback {
        /**
         * Called when the camera auto focus completes.  If the camera
         * does not support auto-focus and autoFocus is called,
         * onAutoFocus will be called immediately with a fake value of
         * <code>success</code> set to <code>true</code>.
         * <p/>
         * The auto-focus routine does not lock auto-exposure and auto-white
         * balance after it completes.
         *
         * @param success true if focus was successful, false if otherwise
         */
        void onAutoFocus(boolean success);
    }

    /**
     * Callback interface used to notify on auto focus start and stop.
     * <p/>
     * <p>This is only supported in continuous autofocus modes -- {@link
     * Camera.Parameters#FOCUS_MODE_CONTINUOUS_VIDEO} and {@link
     * Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE}. Applications can show
     * autofocus animation based on this.</p>
     */
    public interface AutoFocusMoveCallback {
        /**
         * Called when the camera auto focus starts or stops.
         *
         * @param start true if focus starts to move, false if focus stops to move
         */
        void onAutoFocusMoving(boolean start);
    }

    //==============================================================================================
    // Public
    //==============================================================================================

    /**
     * Stops the camera and releases the resources of the camera and underlying detector.
     */
    public void release() {
        synchronized (cameraLock) {
            stop();
            frameProcessor.release();
        }
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector.  The preview
     * frames are not displayed.
     *
     * @throws IOException if the camera's preview texture or display could not be initialized
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    public CameraSource start() throws IOException {
        synchronized (cameraLock) {
            if (camera != null) {
                return this;
            }

            camera = createCamera();

            // SurfaceTexture was introduced in Honeycomb (11), so if we are running and
            // old version of Android. fall back to use SurfaceView.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                dummySurfaceTexture = new SurfaceTexture(DUMMY_TEXTURE_NAME);
                camera.setPreviewTexture(dummySurfaceTexture);
            } else {
                dummySurfaceView = new SurfaceView(context);
                camera.setPreviewDisplay(dummySurfaceView.getHolder());
            }
            camera.startPreview();

            processingThread = new Thread(frameProcessor);
            frameProcessor.setActive(true);
            processingThread.start();
        }
        return this;
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector.  The supplied
     * surface holder is used for the preview so frames can be displayed to the user.
     *
     * @param surfaceHolder the surface holder to use for the preview frames
     * @throws IOException if the supplied surface holder could not be used as the preview display
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    public CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        synchronized (cameraLock) {
            if (camera != null) {
                return this;
            }

            camera = createCamera();
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            processingThread = new Thread(frameProcessor);
            frameProcessor.setActive(true);
            processingThread.start();
        }
        return this;
    }

    /**
     * Closes the camera and stops sending frames to the underlying frame detector.
     * <p/>
     * This camera source may be restarted again by calling {@link #start()} or
     * {@link #start(SurfaceHolder)}.
     * <p/>
     * Call {@link #release()} instead to completely shut down this camera source and release the
     * resources of the underlying detector.
     */
    public void stop() {
        synchronized (cameraLock) {
            frameProcessor.setActive(false);
            if (processingThread != null) {
                try {
                    // Wait for the thread to complete to ensure that we can't have multiple threads
                    // executing at the same time (i.e., which would happen if we called start too
                    // quickly after stop).
                    processingThread.join();
                } catch (InterruptedException e) {
                    Log.d(TAG, "Frame processing thread interrupted on release.");
                }
                processingThread = null;
            }

            // clear the buffer to prevent oom exceptions
            bytesToByteBuffer.clear();

            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallbackWithBuffer(null);
                try {
                    // We want to be compatible back to Gingerbread, but SurfaceTexture
                    // wasn't introduced until Honeycomb.  Since the interface cannot use a SurfaceTexture, if the
                    // developer wants to display a preview we must use a SurfaceHolder.  If the developer doesn't
                    // want to display a preview we use a SurfaceTexture if we are running at least Honeycomb.

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        camera.setPreviewTexture(null);
                    } else {
                        camera.setPreviewDisplay(null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear camera preview: " + e);
                }
                camera.release();
                camera = null;
            }
        }
    }

    /**
     * Returns the preview size that is currently in use by the underlying camera.
     */
    public Size getPreviewSize() {
        return previewSize;
    }

    /**
     * Returns the selected camera; one of {@link #CAMERA_FACING_BACK} or
     * {@link #CAMERA_FACING_FRONT}.
     */
    public int getCameraFacing() {
        return facing;
    }

    public int doZoom(float scale) {
        synchronized (cameraLock) {
            if (camera == null) {
                return 0;
            }
            int currentZoom = 0;
            int maxZoom;
            Camera.Parameters parameters = camera.getParameters();
            if (!parameters.isZoomSupported()) {
                Log.w(TAG, "Zoom is not supported on this device");
                return currentZoom;
            }
            maxZoom = parameters.getMaxZoom();

            currentZoom = parameters.getZoom() + 1;
            float newZoom;
            if (scale > 1) {
                newZoom = currentZoom + scale * (maxZoom / 10);
            } else {
                newZoom = currentZoom * scale;
            }
            currentZoom = Math.round(newZoom) - 1;
            if (currentZoom < 0) {
                currentZoom = 0;
            } else if (currentZoom > maxZoom) {
                currentZoom = maxZoom;
            }
            parameters.setZoom(currentZoom);
            camera.setParameters(parameters);
            return currentZoom;
        }
    }

    /**
     * Initiates taking a picture, which happens asynchronously.  The camera source should have been
     * activated previously with {@link #start()} or {@link #start(SurfaceHolder)}.  The camera
     * preview is suspended while the picture is being taken, but will resume once picture taking is
     * done.
     *
     * @param shutter the callback for image capture moment, or null
     * @param jpeg    the callback for JPEG image data, or null
     */
    public void takePicture(ShutterCallback shutter, PictureCallback jpeg) {
        synchronized (cameraLock) {
            if (camera != null) {
                PictureStartCallback startCallback = new PictureStartCallback();
                startCallback.delegate = shutter;
                PictureDoneCallback doneCallback = new PictureDoneCallback();
                doneCallback.delegate = jpeg;
                camera.takePicture(startCallback, null, null, doneCallback);
            }
        }
    }

    /**
     * Gets the current focus mode setting.
     *
     * @return current focus mode. This value is null if the camera is not yet created. Applications should call {@link
     * #autoFocus(AutoFocusCallback)} to start the focus if focus
     * mode is FOCUS_MODE_AUTO or FOCUS_MODE_MACRO.
     * @see Camera.Parameters#FOCUS_MODE_AUTO
     * @see Camera.Parameters#FOCUS_MODE_INFINITY
     * @see Camera.Parameters#FOCUS_MODE_MACRO
     * @see Camera.Parameters#FOCUS_MODE_FIXED
     * @see Camera.Parameters#FOCUS_MODE_EDOF
     * @see Camera.Parameters#FOCUS_MODE_CONTINUOUS_VIDEO
     * @see Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE
     */
    @Nullable
    @FocusMode
    public String getFocusMode() {
        return focusMode;
    }

    /**
     * Sets the focus mode.
     *
     * @param mode the focus mode
     * @return {@code true} if the focus mode is set, {@code false} otherwise
     * @see #getFocusMode()
     */
    public boolean setFocusMode(@FocusMode String mode) {
        synchronized (cameraLock) {
            if (camera != null && mode != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters.getSupportedFocusModes().contains(mode)) {
                    parameters.setFocusMode(mode);
                    camera.setParameters(parameters);
                    focusMode = mode;
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Gets the current flash mode setting.
     *
     * @return current flash mode. null if flash mode setting is not
     * supported or the camera is not yet created.
     * @see Camera.Parameters#FLASH_MODE_OFF
     * @see Camera.Parameters#FLASH_MODE_AUTO
     * @see Camera.Parameters#FLASH_MODE_ON
     * @see Camera.Parameters#FLASH_MODE_RED_EYE
     * @see Camera.Parameters#FLASH_MODE_TORCH
     */
    @Nullable
    @FlashMode
    public String getFlashMode() {
        return flashMode;
    }

    /**
     * Sets the flash mode.
     *
     * @param mode flash mode.
     * @return {@code true} if the flash mode is set, {@code false} otherwise
     * @see #getFlashMode()
     */
    public boolean setFlashMode(@FlashMode String mode) {
        synchronized (cameraLock) {
            if (camera != null && mode != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters.getSupportedFlashModes().contains(mode)) {
                    parameters.setFlashMode(mode);
                    camera.setParameters(parameters);
                    flashMode = mode;
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Starts camera auto-focus and registers a callback function to run when
     * the camera is focused.  This method is only valid when preview is active
     * (between {@link #start()} or {@link #start(SurfaceHolder)} and before {@link #stop()} or {@link #release()}).
     * <p/>
     * <p>Callers should check
     * {@link #getFocusMode()} to determine if
     * this method should be called. If the camera does not support auto-focus,
     * it is a no-op and {@link AutoFocusCallback#onAutoFocus(boolean)}
     * callback will be called immediately.
     * <p/>
     * <p>If the current flash mode is not
     * {@link Camera.Parameters#FLASH_MODE_OFF}, flash may be
     * fired during auto-focus, depending on the driver and camera hardware.<p>
     *
     * @param cb the callback to run
     * @see #cancelAutoFocus()
     */
    public void autoFocus(@Nullable AutoFocusCallback cb) {
        synchronized (cameraLock) {
            if (camera != null) {
                CameraAutoFocusCallback autoFocusCallback = null;
                if (cb != null) {
                    autoFocusCallback = new CameraAutoFocusCallback();
                    autoFocusCallback.delegate = cb;
                }
                camera.autoFocus(autoFocusCallback);
            }
        }
    }

    /**
     * Cancels any auto-focus function in progress.
     * Whether or not auto-focus is currently in progress,
     * this function will return the focus position to the default.
     * If the camera does not support auto-focus, this is a no-op.
     *
     * @see #autoFocus(AutoFocusCallback)
     */
    public void cancelAutoFocus() {
        synchronized (cameraLock) {
            if (camera != null) {
                camera.cancelAutoFocus();
            }
        }
    }

    /**
     * Sets camera auto-focus move callback.
     *
     * @param cb the callback to run
     * @return {@code true} if the operation is supported (i.e. from Jelly Bean), {@code false} otherwise
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean setAutoFocusMoveCallback(@Nullable AutoFocusMoveCallback cb) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }

        synchronized (cameraLock) {
            if (camera != null) {
                CameraAutoFocusMoveCallback autoFocusMoveCallback = null;
                if (cb != null) {
                    autoFocusMoveCallback = new CameraAutoFocusMoveCallback();
                    autoFocusMoveCallback.delegate = cb;
                }
                camera.setAutoFocusMoveCallback(autoFocusMoveCallback);
            }
        }

        return true;
    }

    //==============================================================================================
    // Private
    //==============================================================================================

    /**
     * Only allow creation via the builder class.
     */
    private CameraSource() {
    }

    /**
     * Wraps the camera1 shutter callback so that the deprecated API isn't exposed.
     */
    private class PictureStartCallback implements Camera.ShutterCallback {

        private ShutterCallback delegate;

        @Override
        public void onShutter() {
            if (delegate != null) {
                delegate.onShutter();
            }
        }
    }

    /**
     * Wraps the final callback in the camera sequence, so that we can automatically turn the camera
     * preview back on after the picture has been taken.
     */
    private class PictureDoneCallback implements Camera.PictureCallback {

        private PictureCallback delegate;

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (delegate != null) {
                delegate.onPictureTaken(data);
            }
            synchronized (cameraLock) {
                if (CameraSource.this.camera != null) {
                    CameraSource.this.camera.startPreview();
                }
            }
        }
    }

    /**
     * Wraps the camera1 auto focus callback so that the deprecated API isn't exposed.
     */
    private class CameraAutoFocusCallback implements Camera.AutoFocusCallback {

        private AutoFocusCallback delegate;

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (delegate != null) {
                delegate.onAutoFocus(success);
            }
        }
    }

    /**
     * Wraps the camera1 auto focus move callback so that the deprecated API isn't exposed.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private class CameraAutoFocusMoveCallback implements Camera.AutoFocusMoveCallback {

        private AutoFocusMoveCallback delegate;

        @Override
        public void onAutoFocusMoving(boolean start, Camera camera) {
            if (delegate != null) {
                delegate.onAutoFocusMoving(start);
            }
        }
    }

    /**
     * Opens the camera and applies the user settings.
     *
     * @throws RuntimeException if the method fails
     */
    @SuppressLint("InlinedApi")
    private Camera createCamera() {
        int requestedCameraId = getIdForRequestedCamera(facing);
        if (requestedCameraId == -1) {
            throw new RuntimeException("Could not find requested camera.");
        }
        Camera camera = Camera.open(requestedCameraId);

        SizePair sizePair = selectSizePair(camera, requestedPreviewWidth, requestedPreviewHeight);
        if (sizePair == null) {
            throw new RuntimeException("Could not find suitable preview size.");
        }
        Size pictureSize = sizePair.pictureSize();
        previewSize = sizePair.previewSize();

        int[] previewFpsRange = selectPreviewFpsRange(camera, requestedFps);
        if (previewFpsRange == null) {
            throw new RuntimeException("Could not find suitable preview frames per second range.");
        }

        Camera.Parameters parameters = camera.getParameters();

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        }

        parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        parameters.setPreviewFpsRange(previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
        );
        parameters.setPreviewFormat(ImageFormat.NV21);

        setRotation(camera, parameters, requestedCameraId);

        if (focusMode != null) {
            if (parameters.getSupportedFocusModes().contains(focusMode)) {
                parameters.setFocusMode(focusMode);
            } else {
                Log.i(TAG, "Camera focus mode: " + focusMode + " is not supported on this device.");
            }
        }

        // setting focusMode to the one set in the params
        focusMode = parameters.getFocusMode();

        if (flashMode != null) {
            if (parameters.getSupportedFlashModes() != null) {
                if (parameters.getSupportedFlashModes().contains(flashMode)) {
                    parameters.setFlashMode(flashMode);
                } else {
                    Log.i(TAG, "Camera flash mode: " + flashMode + " is not supported on this device.");
                }
            }
        }

        // setting flashMode to the one set in the params
        flashMode = parameters.getFlashMode();

        camera.setParameters(parameters);

        // Four frame buffers are needed for working with the camera:
        //
        //   one for the frame that is currently being executed upon in doing detection
        //   one for the next pending frame to process immediately upon completing detection
        //   two for the frames that the camera uses to populate future preview images
        camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));

        return camera;
    }

    /**
     * Gets the id for the camera specified by the direction it is facing.  Returns -1 if no such
     * camera was found.
     *
     * @param facing the desired camera (front-facing or rear-facing)
     */
    private static int getIdForRequestedCamera(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Selects the most suitable preview and picture size, given the desired width and height.
     * <p/>
     * Even though we may only need the preview size, it's necessary to find both the preview
     * size and the picture size of the camera together, because these need to have the same aspect
     * ratio.  On some hardware, if you would only set the preview size, you will get a distorted
     * image.
     *
     * @param camera        the camera to select a preview size from
     * @param desiredWidth  the desired width of the camera preview frames
     * @param desiredHeight the desired height of the camera preview frames
     * @return the selected preview and picture size pair
     */
    private static SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);

        // The method for selecting the best size is to minimize the sum of the differences between
        // the desired values and the actual values for width and height.  This is certainly not the
        // only way to select the best size, but it provides a decent tradeoff between using the
        // closest aspect ratio vs. using the closest pixel area.
        SizePair selectedPair = null;
        int minDiff = Integer.MAX_VALUE;
        for (SizePair sizePair : validPreviewSizes) {
            Size size = sizePair.previewSize();
            int diff = Math.abs(size.getWidth() - desiredWidth) +
                    Math.abs(size.getHeight() - desiredHeight);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    /**
     * Stores a preview size and a corresponding same-aspect-ratio picture size.  To avoid distorted
     * preview images on some devices, the picture size must be set to a size that is the same
     * aspect ratio as the preview size or the preview may end up being distorted.  If the picture
     * size is null, then there is no picture size with the same aspect ratio as the preview size.
     */
    private static class SizePair {

        private Size preview;
        private Size picture;

        public SizePair(
                android.hardware.Camera.Size previewSize,
                android.hardware.Camera.Size pictureSize
        ) {
            preview = new Size(previewSize.width, previewSize.height);
            if (pictureSize != null) {
                picture = new Size(pictureSize.width, pictureSize.height);
            }
        }

        public Size previewSize() {
            return preview;
        }

        @SuppressWarnings("unused")
        public Size pictureSize() {
            return picture;
        }
    }

    /**
     * Generates a list of acceptable preview sizes.  Preview sizes are not acceptable if there is
     * not a corresponding picture size of the same aspect ratio.  If there is a corresponding
     * picture size of the same aspect ratio, the picture size is paired up with the preview size.
     * <p/>
     * This is necessary because even if we don't use still pictures, the still picture size must be
     * set to a size that is the same aspect ratio as the preview size we choose.  Otherwise, the
     * preview images may be distorted on some devices.
     */
    private static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<android.hardware.Camera.Size> supportedPreviewSizes =
                parameters.getSupportedPreviewSizes();
        List<android.hardware.Camera.Size> supportedPictureSizes =
                parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList<>();
        for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // By looping through the picture sizes in order, we favor the higher resolutions.
            // We choose the highest resolution in order to support taking the full resolution
            // picture later.
            for (android.hardware.Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all
        // of the preview sizes and hope that the camera can handle it.  Probably unlikely, but we
        // still account for it.
        if (validPreviewSizes.size() == 0) {
            Log.w(TAG, "No preview sizes have a corresponding same-aspect-ratio picture size");
            for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
                // The null picture size will let us know that we shouldn't set a picture size.
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    /**
     * Selects the most suitable preview frames per second range, given the desired frames per
     * second.
     *
     * @param camera            the camera to select a frames per second range from
     * @param desiredPreviewFps the desired frames per second for the camera preview frames
     * @return the selected preview frames per second range
     */
    private int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        // The camera API uses integers scaled by a factor of 1000 instead of floating-point frame
        // rates.
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        // The method for selecting the best range is to minimize the sum of the differences between
        // the desired value and the upper and lower bounds of the range.  This may select a range
        // that the desired value is outside of, but this is often preferred.  For example, if the
        // desired frame rate is 29.97, the range (30, 30) is probably more desirable than the
        // range (15, 30).
        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }

    /**
     * Calculates the correct rotation for the given camera id and sets the rotation in the
     * parameters.  It also sets the camera's display orientation and rotation.
     *
     * @param parameters the camera parameters for which to set the rotation
     * @param cameraId   the camera id to set rotation based on
     */
    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                Log.e(TAG, "Bad rotation value: " + rotation);
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int angle;
        int displayAngle;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - angle) % 360; // compensate for it being mirrored
        } else {  // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }

        // This corresponds to the rotation constants in {@link Frame}.
        this.rotation = angle / 90;

        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(angle);
    }

    /**
     * Creates one buffer for the camera preview callback.  The size of the buffer is based off of
     * the camera preview size and the format of the camera image.
     *
     * @return a new preview buffer of the appropriate size for the current camera settings
     */
    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        //
        // NOTICE: This code only works when using play services v. 8.1 or higher.
        //

        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        bytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    //==============================================================================================
    // Frame processing
    //==============================================================================================

    /**
     * Called when the camera has a new preview frame.
     */
    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            frameProcessor.setNextFrame(data, camera);
        }
    }

    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera.  This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     * <p/>
     * While detection is running on a frame, new frames may be received from the camera.  As these
     * frames come in, the most recent frame is held onto as pending.  As soon as detection and its
     * associated processing are done for the previous frame, detection on the mostly recently
     * received frame will immediately start on the same thread.
     */
    private class FrameProcessingRunnable implements Runnable {

        private Detector<?> detector;
        private long startTimeMillis = SystemClock.elapsedRealtime();

        // This lock guards all of the member variables below.
        private final Object lock = new Object();
        private boolean active = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        private long pendingTimeMillis;
        private int pendingFrameId = 0;
        private ByteBuffer pendingFrameData;

        FrameProcessingRunnable(Detector<?> detector) {
            this.detector = detector;
        }

        /**
         * Releases the underlying receiver.  This is only safe to do after the associated thread
         * has completed, which is managed in camera source's release method above.
         */
        @SuppressLint("Assert")
        void release() {
            assert (processingThread.getState() == Thread.State.TERMINATED);
            detector.release();
            detector = null;
        }

        /**
         * Marks the runnable as active/not active.  Signals any blocked threads to continue.
         */
        void setActive(boolean active) {
            synchronized (lock) {
                this.active = active;
                lock.notifyAll();
            }
        }

        /**
         * Sets the frame data received from the camera.  This adds the previous unused frame buffer
         * (if present) back to the camera, and keeps a pending reference to the frame data for
         * future use.
         */
        void setNextFrame(byte[] data, Camera camera) {
            synchronized (lock) {
                if (pendingFrameData != null) {
                    camera.addCallbackBuffer(pendingFrameData.array());
                    pendingFrameData = null;
                }

                if (!bytesToByteBuffer.containsKey(data)) {
                    Log.d(TAG,
                            "Skipping frame.  Could not find ByteBuffer associated with the image " + "data from the camera."
                    );
                    return;
                }

                // Timestamp and frame ID are maintained here, which will give downstream code some
                // idea of the timing of frames received and when frames were dropped along the way.
                pendingTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;
                pendingFrameId++;
                pendingFrameData = bytesToByteBuffer.get(data);

                // Notify the processor thread if it is waiting on the next frame (see below).
                lock.notifyAll();
            }
        }

        /**
         * As long as the processing thread is active, this executes detection on frames
         * continuously.  The next pending frame is either immediately available or hasn't been
         * received yet.  Once it is available, we transfer the frame info to local variables and
         * run detection on that frame.  It immediately loops back for the next frame without
         * pausing.
         * <p/>
         * If detection takes longer than the time in between new frames from the camera, this will
         * mean that this loop will run without ever waiting on a frame, avoiding any context
         * switching or frame acquisition time latency.
         * <p/>
         * If you find that this is using more CPU than you'd like, you should probably decrease the
         * FPS setting above to allow for some idle time in between frames.
         */
        @Override
        public void run() {
            Frame outputFrame;
            ByteBuffer data;

            while (true) {
                synchronized (lock) {
                    while (active && (pendingFrameData == null)) {
                        try {
                            // Wait for the next frame to be received from the camera, since we
                            // don't have it yet.
                            lock.wait();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Frame processing loop terminated.", e);
                            return;
                        }
                    }

                    if (!active) {
                        // Exit the loop once this camera source is stopped or released.  We check
                        // this here, immediately after the wait() above, to handle the case where
                        // setActive(false) had been called, triggering the termination of this
                        // loop.
                        return;
                    }

                    outputFrame = new Frame.Builder().setImageData(pendingFrameData,
                                                             previewSize.getWidth(),
                                                             previewSize.getHeight(),
                                                             ImageFormat.NV21
                                                     )
                                                     .setId(pendingFrameId)
                                                     .setTimestampMillis(pendingTimeMillis)
                                                     .setRotation(rotation)
                                                     .build();

                    // Hold onto the frame data locally, so that we can use this for detection
                    // below.  We need to clear pendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    data = pendingFrameData;
                    pendingFrameData = null;
                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    detector.receiveFrame(outputFrame);
                } catch (Throwable t) {
                    Log.e(TAG, "Exception thrown from receiver.", t);
                } finally {
                    camera.addCallbackBuffer(data.array());
                }
            }
        }
    }
}
