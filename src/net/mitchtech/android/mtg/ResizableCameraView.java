package net.mitchtech.android.mtg;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

public class ResizableCameraView extends JavaCameraView {
    public ResizableCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }
}
