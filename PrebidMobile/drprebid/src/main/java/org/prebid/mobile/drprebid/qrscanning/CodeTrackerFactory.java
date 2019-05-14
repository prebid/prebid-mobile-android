package org.prebid.mobile.drprebid.qrscanning;

import android.content.Context;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import org.prebid.mobile.drprebid.qrscanning.camera.GraphicOverlay;

public class CodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay<CodeGraphic> mGraphicOverlay;
    private Context mContext;

    public CodeTrackerFactory(GraphicOverlay<CodeGraphic> mGraphicOverlay,
                              Context mContext) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.mContext = mContext;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        CodeGraphic graphic = new CodeGraphic(mGraphicOverlay);
        return new CodeGraphicTracker(mGraphicOverlay, graphic, mContext);
    }
}
