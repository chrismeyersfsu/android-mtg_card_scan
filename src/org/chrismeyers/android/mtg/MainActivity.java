
package org.chrismeyers.android.mtg;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.chrismeyers.android.mtg.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    public static final int MODE_ONLY_DISPLAY_MATCH = 1;
    public static final int MODE_PROCESS_EVERY_N_FRAME = 4;

    public static final int CAMERA_WIDTH = 320;
    public static final int CAMERA_HEIGHT = 240;
    
//    public static final int CAMERA_WIDTH = 640;
//    public static final int CAMERA_HEIGHT = 480;


    private int mFrameCount = 0;
    private ResizableCameraView mOpenCvCameraView;
    private ImageView mSnapshotView = null;
    private Canvas mSnapshotCanvas = null;
    private Boolean firstTime = true;
    private Bitmap mSnapshotBitmap = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                    break;
                default:
                {
                    super.onManagerConnected(status);
                }
                    break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_activity);

        mOpenCvCameraView = (ResizableCameraView) findViewById(R.id.cameraView);
        mOpenCvCameraView.setMaxFrameSize(CAMERA_WIDTH, CAMERA_HEIGHT);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mSnapshotView = (ImageView) findViewById(R.id.snapshotView);
        mSnapshotView.setVisibility(SurfaceView.VISIBLE);
        mSnapshotCanvas = new Canvas();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public boolean sizesMatch(final Bitmap bitmap, final Mat mat) {
        if (bitmap.getHeight() != mat.rows() || bitmap.getWidth() != mat.cols()) {
            return false;
        }
        return true;
    }

    public void updateSnapshotFrame(Mat imgCard) {
        // If needed, resize bitmap
        // Note: resizing the bitmap and reassigning the imageview bitmap is an
        // inefficient operation and can lead to slow-downs
        if (mSnapshotBitmap == null || sizesMatch(mSnapshotBitmap, imgCard) == false) {
            mSnapshotBitmap = Bitmap.createBitmap(imgCard.cols(), imgCard.rows(),
                    Bitmap.Config.ARGB_8888);
            setImage(mSnapshotView, mSnapshotBitmap);
        }
        Utils.matToBitmap(imgCard, mSnapshotBitmap);
        mSnapshotView.postInvalidate();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat gray = inputFrame.gray();
        Mat imgCard = new Mat(310, 223, gray.type());
        Mat liveMat = new Mat();
        

        if (mFrameCount % MODE_PROCESS_EVERY_N_FRAME == 0) {
            int res = Card.findCard(gray, imgCard);
            if (MODE_ONLY_DISPLAY_MATCH == 0
                    || (MODE_ONLY_DISPLAY_MATCH == 1 && res == Card.RECTANGLE_FOUND)) {
                updateSnapshotFrame(imgCard);
            }
        }
        mFrameCount++;

        liveMat = inputFrame.rgba();
        return liveMat;
    }

    public void setImage(final ImageView imageView, final Bitmap img) {
        runOnUiThread(new Runnable() {
            public void run() {
                imageView.setImageBitmap(img);
                imageView.invalidate();
            }
        });
    }
}
