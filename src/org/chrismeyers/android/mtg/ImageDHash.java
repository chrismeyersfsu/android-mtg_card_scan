package org.chrismeyers.android.mtg;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class ImageDHash {
    
    static Mat calcDctHash(Mat imgIn) {
        Mat imgFloat = new Mat(imgIn.size(), CvType.CV_32FC1, new Scalar(1));
        Imgproc.resize(imgIn, imgFloat, null, 1/255.0, 1/255.0, Imgproc.INTER_NEAREST);
        Mat small_img = new Mat(new Size(32, 32), CvType.CV_32FC1);
        
        Mat dct = new Mat(new Size(32, 32), CvType.CV_32FC1);
        Core.dct(small_img, dct, Core.DCT_ROWS);
        dct = dct.submat(0, 9, 0, 9);
        
        Scalar avg = Core.mean(dct);
        
        return new Mat();
    }
    /*
    img = float_version(img)
            small_img = cv.CreateImage((32, 32), 32, 1)
            cv.Resize(img[20:190, 20:205], small_img)

            dct = cv.CreateMat(32, 32, cv.CV_32FC1)
            cv.DCT(small_img, dct, cv.CV_DXT_FORWARD)
            dct = dct[1:9, 1:9]

            avg = cv.Avg(dct)[0]
            dct_bit = cv.CreateImage((8,8),8,1)
            cv.CmpS(dct, avg, dct_bit, cv.CV_CMP_GT)

            return [dct_bit[y, x]==255.0
                    for y in xrange(8)
                    for x in xrange(8)]
                    */
}
