package org.prebid.mobile.drprebid.qrscanning;

import android.content.Context;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import org.prebid.mobile.drprebid.qrscanning.camera.GraphicOverlay;

public class CodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private GraphicOverlay<CodeGraphic> graphicOverlay;
    private Context context;

    public CodeTrackerFactory(
            GraphicOverlay<CodeGraphic> graphicOverlay,
            Context context
    ) {
        this.graphicOverlay = graphicOverlay;
        this.context = context;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        CodeGraphic graphic = new CodeGraphic(graphicOverlay);
        return new CodeGraphicTracker(graphicOverlay, graphic, context);
    }

}
