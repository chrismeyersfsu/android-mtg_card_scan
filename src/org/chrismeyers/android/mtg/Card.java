
package org.chrismeyers.android.mtg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.util.Log;

public class Card {
    public static final String TAG = "Card";
    public static final double CARD_AREA_MIN = 10000;
    public static final Scalar COLOR_YELLOW = new Scalar(255, 255, 0);

    public static final int RECTANGLE_NOT_FOUND_CANNY = 1;
    public static final int RECTANGLE_COULD_NOT_ROTATE = 2;
    public static final int RECTANGLE_FOUND = 3;
    public static final int RECTANGLE_OUT_OF_SCENE = 4;

    public static final int IMG_PROCESS_WIDTH = 320;
    public static final int IMG_PROCESS_HEIGHT = 240;

    /**
     * Given an image, attempts to find a card in it
     * 
     * @return
     */
    static ArrayList<Integer> detect(Mat imageGray) {
        List<MatOfPoint> edgeContours = new ArrayList<MatOfPoint>();
        Mat imgCanny = ImageCanny(imageGray);
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgCanny, edgeContours, hierarchy, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        Log.d(TAG, "Contour size:");
        for (MatOfPoint point : edgeContours) {

            Log.d(TAG,
                    "\tcontour size " + point.width() + "x" + point.height() + " - " + point.cols()
                            + "x" + point.rows());
        }

        return null;

    }

    static Mat ImageCanny(Mat imageGray) {
        Mat imgCanny = imageGray.clone();
        Imgproc.Canny(imageGray, imgCanny, 100, 100);
        return imgCanny;
    }

    /**
     * @param image may be modified
     * @param edgeContours
     * @param hierarchy
     * @param mode
     * @param method
     * @return
     */
    static Mat findAndDrawContours(Mat image, List<MatOfPoint> edgeContours) {
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        int mode = Imgproc.RETR_LIST;
        Mat img = image.clone();
        int contourIdx = -1; // draw all contours
        Mat hierarchy = new Mat();

        /*
         * The canny filter finds the outlines.
         */
        Mat imgCanny = ImageCanny(image);
        /*
         * Finding contours amounts to finding "outside" edges. So of the set of
         * edges found by the canny filter, further reduce them
         */
        Imgproc.findContours(imgCanny, edgeContours, hierarchy, mode, method);
        Scalar color = new Scalar(255, 255, 0);
        Imgproc.drawContours(img, edgeContours, contourIdx, color);
        return img;
    }

    static public class ContourArea {
        MatOfPoint points;
        double area;
    };

    /**
     * Find the single largest contour by area. Optimize the algorithm by
     * skipping computing the area on contours that have rows or columns less
     * than the minimum.
     * 
     * @param points
     * @param minRows
     * @param minCols
     * @return
     */
    static ContourArea findGreatestContourByArea(final List<MatOfPoint> points, final int minRows,
            final int minCols) {
        if (points == null || points.size() == 0) {
            return null;
        }
        ContourArea countourArea = new Card.ContourArea();
        countourArea.points = points.get(0);
        countourArea.area = Imgproc.contourArea(countourArea.points);

        for (MatOfPoint matOfPoint : points) {
            if (matOfPoint.rows() < minRows) {
                continue;
            }

            if (matOfPoint.cols() < minCols) {
                continue;
            }

            double areaTmp = Imgproc.contourArea(matOfPoint);
            if (areaTmp > countourArea.area) {
                countourArea.points = matOfPoint;
                countourArea.area = areaTmp;
            }
        }
        return countourArea;
    }

    /**
     * Note the logic here is flawed. We can't just look at the row count Find
     * the single largest contour by area. More accurate results obtained by
     * discarding rows that are larger than the image size.
     * 
     * @param points
     * @param minRows
     * @param minCols
     * @return
     */
    static MatOfPoint findGreatestContourByRow(List<MatOfPoint> points, int maxRows) {
        if (points == null || points.size() == 0) {
            return null;
        }
        MatOfPoint greatestPoint = points.get(0);
        int greatestRows = greatestPoint.rows();

        for (MatOfPoint matOfPoint : points) {
            int rows = matOfPoint.rows();
            if (rows > maxRows) {
                continue;
            }
            if (rows > greatestRows) {
                greatestPoint = matOfPoint;
            }
        }
        return greatestPoint;
    }

    static boolean resizeIfNeeded(Mat img, int desiredWidth, int desiredHeight) {
        Size size = img.size();
        Size desiredSize = new Size(desiredWidth, desiredHeight);
        if (size.width != desiredWidth || size.height != desiredHeight) {
            Imgproc.resize(img, img, desiredSize);
            return true;
        }
        return false;
    }

    static void drawContour(Mat image, MatOfPoint contour, Scalar lineColor) {
        ArrayList<MatOfPoint> tmp = new ArrayList<MatOfPoint>();
        tmp.add(contour);
        Imgproc.drawContours(image, tmp, -1, lineColor);
    }

    static void drawLines(Mat img, MatOfPoint points, boolean isClosed, Scalar color) {
        List<MatOfPoint> pointsArray = new ArrayList<MatOfPoint>();
        pointsArray.add(points);
        Core.polylines(img, pointsArray, isClosed, color);
    }
    
    static void drawRect(Mat img, Rect rect, Scalar color) {
        Point[] points = new Point[4];
        RotatedRect rectRotated = new RotatedRect(new Point(rect.x+(rect.width/2),rect.y+(rect.height/2)), rect.size(), 0);
        rectRotated.points(points);
        
        MatOfPoint points2 = new MatOfPoint(points);
        drawLines(img, points2, true, color);
    }

    static void drawLines(Mat img, Point[] points, boolean isClosed, Scalar color) {
        List<MatOfPoint> pointsArray = new ArrayList<MatOfPoint>();
        MatOfPoint matPoint = new MatOfPoint(points);
        pointsArray.add(matPoint);
        Core.polylines(img, pointsArray, isClosed, color);
    }

    static double distance(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    static double distance(Point p1, Point p2)
    {
        return Math.sqrt(Math.pow((p2.x - p1.x), 2) + Math.pow((p2.y - p1.y), 2));
    }

    static double slope(Point p1, Point p2) {
        return (p2.y - p1.y) / (p2.x - p1.x);
    }

    static boolean riseIsUp(Point p1, Point p2) {
        if (p2.y < p1.y) {
            return true;
        }
        return false;
    }

    static boolean riseIsDown(Point p1, Point p2) {
        return !(riseIsUp(p1, p2));
    }

    /**
     * Rotates an image. The new image size is max(height,width) x
     * max(height,width) of the input image.
     * 
     * @param imgIn
     * @param imgOut
     * @param angle
     */
    static void rotateImageLarge(Mat imgIn, Mat imgOut, double angle) {
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(new
                Point(imgIn.width() / 2, imgIn.height() / 2), angle, 1.0);
        Size imgSize = imgIn.size();
        int maxSize = (int)
                Math.max((double) imgSize.height, (double)
                        imgSize.width);
        Size finalSize = new Size(maxSize, maxSize);

        Imgproc.warpAffine(imgIn, imgOut, rotationMatrix,
                finalSize);
    }

    static void transformPerspective(Point[] tightOutline, Point[] boundingCorners,
            Mat imgIn, Mat imgOut) {
        MatOfPoint2f src = new MatOfPoint2f(tightOutline);
        MatOfPoint2f dest = new MatOfPoint2f(boundingCorners);

        Mat transform = Imgproc.getPerspectiveTransform(src, dest);
        /*
         * TODO: the last parameter to warpPerspective() can crop the image.
         * Consider cropping the image to the boundingCorners size, maybe by
         * passing in the imgOut to be the boundingCorners size, or another
         * param to this function of type Size
         */
        /*
         * double maxRows = 0, maxCols = 0; for(int
         * i=0;i<boundingCorners.length;i++) { if(maxRows <
         * boundingCorners[i].y) maxRows = boundingCorners[i].y; if(maxCols <
         * boundingCorners[i].x) maxCols = boundingCorners[i].x; }
         */
        Size imgSize = imgIn.size();
        int maxSize = (int)
                Math.max((double) imgSize.height, (double)
                        imgSize.width);
        Size finalSize = new Size(maxSize, maxSize);
        Imgproc.warpPerspective(imgIn, imgOut, transform, finalSize);
    }

    /**
     * Given a bunch of points that, generally, follow a rectangular pattern
     * (i.e. a contour), return four corners in a clock-wise ordering from
     * top-left. Note this method doesn't work well when the "card" is not
     * rotated any and has a skewed perspective. We get, what can be thought of
     * as, a false positive.
     * 
     * @param points
     * @return
     */
    static Point[] identifyRectangleCornersByMax(Point[] points) {

        Point greatestX = new Point(0, 0);
        Point leastX = new Point(Double.MAX_VALUE, 0);
        Point greatestY = new Point(0, 0);
        Point leastY = new Point(0, Double.MAX_VALUE);
        Point[] corners = new Point[4];

        for (int i = 0; i < points.length; ++i) {
            greatestX = points[i].x > greatestX.x ? points[i] : greatestX;
            leastX = points[i].x < leastX.x ? points[i] : leastX;
            greatestY = points[i].y > greatestY.y ? points[i] : greatestY;
            leastY = points[i].y < leastY.y ? points[i] : leastY;
        }

        corners[0] = greatestX;
        corners[1] = leastX;
        corners[2] = greatestY;
        corners[3] = leastY;

        Log.d("CORNERS", "Candidates " + greatestX.x + " " + greatestX.y + "\n" + leastX.x + " "
                + leastX.y + "\n" + greatestY.x + " " + greatestY.y + "\n" + leastY.x + " "
                + leastY.y);

        return orderCorners(corners);
    }

    static Point[] identifyRectangleCornersByApprox(Point[] points) {
        MatOfPoint2f new_mat = new MatOfPoint2f(points);
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        int contourSize = points.length;
        Imgproc.approxPolyDP(new_mat, approxCurve, contourSize * 0.05, true);

        if (approxCurve.total() != 4) {
            return null;
        }
        return approxCurve.toArray();
    }
    
    static Point[] orderCorners(MatOfPoint2f corners) {
        return orderCorners(corners.toArray());
    }
    
    static Point[] orderCorners(MatOfPoint corners) {
        return orderCorners(corners.toArray());
    }
    
    /**
     * Given 4 points (corners). Arrange to points in order: top-left,
     * top-right, bottom-right, bottom-left
     * 
     * @param corners
     * @return 4 ordered points
     */
    static Point[] orderCorners(Point[] cornersUnordered) {
        Point[] cornerPoints = new Point[4];
        Point p1, p2, p3, p4;
        Point topLeft = null, topRight = null, botRight = null, botLeft = null;
        List<Point> corners = new ArrayList<Point>();
        for (int i=0; i < cornersUnordered.length; ++i)
            corners.add(cornersUnordered[i]);
        
        /* Top set of points */
        // find p1
        p1 = corners.get(0);
        for (Point point : corners) {
            if (point.y < p1.y) {
                p1 = point;
            }
        }
        corners.remove(p1);

        // find p2
        p2 = corners.get(0);
        for (Point point : corners) {
            if (distance(p1, point) < distance(p1, p2)) {
                p2 = point;
            }
        }
        corners.remove(p2);

        /* Identify top left and top right */
        /*
         * Note that the logic is safe if the points have equal x values. Safe
         * in the sense that different points will get assigned to topLeft and
         * topRight
         */
        topLeft = p1.x < p2.x ? p1 : p2;
        topRight = p2.x > p1.x ? p2 : p1;

        /* Bottom set of points */
        // corners only contains 2 points, the bottom ones
        p3 = corners.get(0);
        p4 = corners.get(1);
        botRight = p3.x > p4.x ? p3 : p4;
        botLeft = p4.x < p3.x ? p4 : p3;

        cornerPoints[0] = topLeft;
        cornerPoints[1] = topRight;
        cornerPoints[2] = botRight;
        cornerPoints[3] = botLeft;
        
        return cornerPoints;
    }

    /**
     * Angle needed to rotate the rectangle to be at a 90.
     * 
     * @param cornerPoints
     * @return
     */
    // TODO: Raise an error if cornerPoints is not of length 4
    static final int CORNERS_ORDERED = 1;
    static final int CORNERS_UNORDERED = 2;

    static double calcAngleFromCorners(Point[] cornerPoints, int ordering) {
        Point[] orderedCorners = null;
        if (ordering == CORNERS_UNORDERED) {
            orderedCorners = orderCorners(cornerPoints);
        } else {
            orderedCorners = cornerPoints;
        }

        double topSlope = Math.abs(slope(orderedCorners[0], orderedCorners[1]));
        if (Double.isInfinite(topSlope) || Double.isInfinite(-topSlope)) {
            topSlope = 0;
        } else if (riseIsUp(orderedCorners[0], orderedCorners[1]) == true) {
            topSlope *= -1;
        }
        double angle = Math.atan(topSlope) * 180 / Math.PI;
        return angle;
    }

    static void debugPrintCorners(Point[] cornerPoints) {

        Log.d("CORNERS", cornerPoints[0].x + "," + cornerPoints[0].y + " " + cornerPoints[1].x
                + ","
                + cornerPoints[1].y + " " + cornerPoints[2].x + "," + cornerPoints[2].y + " "
                + cornerPoints[3].x + "," + cornerPoints[3].y);
    }
    
    static boolean areAllPointsInImage(Mat img, Point[] points) {
        for (int i = 0; i < points.length; ++i) {
            if (points[i].x < 0 || points[i].x > img.width() || points[i].y < 0
                    || points[i].y > img.height()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Will zone in on the largest rectangle
     * 
     * @param imageGray
     * @return
     */
    static int findCard(Mat imgIn, Mat imgOut) {
        // Mat imgFinal = new Mat(240, 320, imageGray.type()), imgCanny = null;
        Mat imgCanny = null;
        List<MatOfPoint> edgeContours = new ArrayList<MatOfPoint>();
        Card.ContourArea greatestArea = null;
        double angle = 0;
        Mat imgTmp = null;

        RotatedRect sceneRotRect = null;
        Point[] imageCorners = null;
        Point[] sceneCorners = null;
        Point[] points4 = new Point[4];
        Point[] pointPtr = null;

        imgTmp = imgIn.clone();

        resizeIfNeeded(imgTmp, IMG_PROCESS_WIDTH, IMG_PROCESS_HEIGHT);
        imgCanny = ImageCanny(imgTmp);
        Imgproc.GaussianBlur(imgCanny, imgCanny, new Size(15, 15), 15);

        Imgproc.findContours(imgCanny, edgeContours, new Mat(), Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        if (edgeContours.size() == 0) {
            imgCanny.copyTo(imgOut);
            return RECTANGLE_NOT_FOUND_CANNY;
        }

        // find the largest contour box
        greatestArea = findGreatestContourByArea(edgeContours, 120, 0);
        if (greatestArea.points == null || greatestArea.area < CARD_AREA_MIN) {
            imgCanny.copyTo(imgOut);
            return RECTANGLE_NOT_FOUND_CANNY;
        }

        imageCorners = identifyRectangleCornersByApprox(greatestArea.points.toArray());
        if (imageCorners == null) {
            imgCanny.copyTo(imgOut);
            return RECTANGLE_NOT_FOUND_CANNY;
        }
        imageCorners = orderCorners(imageCorners);

        sceneRotRect = Imgproc.minAreaRect(new MatOfPoint2f(
                greatestArea.points.toArray()));
        sceneRotRect.points(points4);
        sceneCorners = orderCorners(points4);
        
        boolean res = areAllPointsInImage(imgCanny, sceneCorners);
        if (res == false) {
            imgCanny.copyTo(imgOut);
            return RECTANGLE_OUT_OF_SCENE;
        }

        angle = calcAngleFromCorners(sceneCorners, Card.CORNERS_ORDERED);

        debugPrintCorners(sceneCorners);
        //Log.d("ROTATE", "angle " + angle);
        
        //imgCanny.copyTo(imgTmp);
        
        
//        Card.drawLines(imgOut, imageCorners, true, COLOR_YELLOW);
//        drawContour(imgOut, greatestArea.points, COLOR_YELLOW);

        if (angle != 0) {
            transformPerspective(imageCorners, sceneCorners,
                    imgTmp, imgTmp);
            rotateImageLarge(imgTmp, imgTmp, angle);
        }
        //Card.drawLines(imgOut, sceneCorners, true, COLOR_YELLOW);

        //pointPtr = CvConvert.rotatePoints(sceneCorners, sceneRect.center, sceneRect.angle);
        RotatedRect tmpRect = new RotatedRect(sceneRotRect.center, sceneRotRect.size, 0);
        tmpRect.points(points4);
        
        
        //Card.drawLines(imgOut, points4, true, COLOR_YELLOW);

        Rect tmpRect2 = new Rect(points4[0], points4[2]);
        // make sure tmpRect2 is inside of the image
        
        Card.drawRect(imgTmp, tmpRect2, COLOR_YELLOW);
        
        Mat imgCrop = new Mat(tmpRect2.size(), imgTmp.type());
        imgCrop = imgTmp.submat(tmpRect2);
        
        Imgproc.resize(imgCrop, imgCrop, imgOut.size());
        
        imgCrop.copyTo(imgOut);
        Log.d("TEST", "final img out size " + imgOut.size());
        //Imgproc.getRectSubPix(imgOut, boundRect.size(), sceneRect.center, imgOut);
        return RECTANGLE_FOUND;
    }

}
