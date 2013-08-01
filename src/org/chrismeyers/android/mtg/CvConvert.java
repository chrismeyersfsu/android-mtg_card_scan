package org.chrismeyers.android.mtg;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvBoost;
import org.opencv.ml.CvSVM;

import android.util.Log;

public class CvConvert {
    static Point[] toPoints(MatOfPoint2f inPoints) {
        return inPoints.toArray();
    }
    
    static Point[] toPoints(Mat inMat) {
        return (new MatOfPoint(inMat)).toArray();
    }
    
    /**
     * p1 - p2
     * @param p1
     * @param p2
     * @return
     */
    static Point subtractPoints(Point p1, Point p2) {
        Point res = new Point();
        
        res.x = p1.x - p2.x;
        res.y = p1.y - p2.y;
        return res;
    }
    
    static Point addPoints(Point p1, Point p2) {
        Point res = new Point();
        
        res.x = p1.x + p2.x;
        res.y = p1.y + p2.y;
        return res;
    }

    static Point[] rotatePoints(Point[] inPoints, Point center, double angle) {
        Point[] results = new Point[inPoints.length];
                
        double theta = angle * 180 / Math.PI;
        for (int i=0; i < inPoints.length; ++i) {
            Point distance = subtractPoints(inPoints[i], center);
            double x = distance.x;
            double y = distance.y;
            //x*cos(alpha) - y*sin(alpha)
            x = x*Math.cos(theta) - y*Math.sin(theta) + center.x;
            //x*sin(alpha) + y*cos(alpha)
            y = x*Math.sin(theta) + y*Math.cos(theta) + center.y;
            
            results[i] = new Point(x, y);
        }
        
        return results;
    }
}