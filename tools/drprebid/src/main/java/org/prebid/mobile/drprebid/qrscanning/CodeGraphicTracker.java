package org.prebid.mobile.drprebid.qrscanning;

import android.content.Context;
import androidx.annotation.UiThread;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import org.prebid.mobile.drprebid.qrscanning.camera.GraphicOverlay;

public class CodeGraphicTracker extends Tracker<Barcode> {

    private GraphicOverlay<CodeGraphic> overlay;
    private CodeGraphic graphic;

    private CodeUpdateListener codeUpdateListener;

    /**
     * Consume the item instance detected from an Activity or Fragment level by implementing the
     * CodeUpdateListener interface method onBarcodeDetected.
     */
    public interface CodeUpdateListener {

        @UiThread
        void onBarcodeDetected(Barcode barcode);
    }

    CodeGraphicTracker(
            GraphicOverlay<CodeGraphic> overlay,
            CodeGraphic graphic,
            Context context
    ) {
        this.overlay = overlay;
        this.graphic = graphic;
        if (context instanceof CodeUpdateListener) {
            this.codeUpdateListener = (CodeUpdateListener) context;
        } else {
            throw new RuntimeException("Hosting activity must implement CodeUpdateListener");
        }
    }

    /**
     * Start tracking the detected item instance within the item overlay.
     */
    @Override
    public void onNewItem(int id, Barcode item) {
        graphic.setId(id);
        codeUpdateListener.onBarcodeDetected(item);
    }

    /**
     * Update the position/characteristics of the item within the overlay.
     */
    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        overlay.add(graphic);
        graphic.updateItem(item);
    }

    /**
     * Hide the graphic when the corresponding object was not detected.  This can happen for
     * intermediate frames temporarily, for example if the object was momentarily blocked from
     * view.
     */
    @Override
    public void onMissing(Detector.Detections<Barcode> detectionResults) {
        overlay.remove(graphic);
    }

    /**
     * Called when the item is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        overlay.remove(graphic);
    }
}
