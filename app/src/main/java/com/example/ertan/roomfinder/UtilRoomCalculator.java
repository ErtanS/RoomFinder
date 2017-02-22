package com.example.ertan.roomfinder;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class UtilRoomCalculator {


    //private static Mat lastFrameRGB = new Mat();
    //private static Mat lastFrameGRAY = new Mat();

    public static ArrayList<ImageBuffer> lastFrames = new ArrayList<>();
    //private static ArrayList<ImageBuffer> frameGrayList = new ArrayList<>();

    //public static Mat mIntermediateMat = new Mat(1080, 1920, CvType.CV_8UC4);
    //public static Mat hierarchy = new Mat();
    //public static List<MatOfPoint> contours = new ArrayList<>();


    /**
     * Blur and find contours in frame
     * @param frameGray Frame to process
     * @return Contours of current frame
     */
    public static ArrayList<MatOfPoint> blurAndCalculateContours(Mat frameGray){
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        ArrayList<double[]> temp = new ArrayList<>();
        Mat mIntermediateMat = new Mat(1080, 1920, CvType.CV_8UC1);
        //Imgproc.bilateralFilter(frameGray,frameGray,);
        Imgproc.GaussianBlur(frameGray, mIntermediateMat, new Size(7, 7), 0);

        //Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
        //Imgproc.erode(frameGray,frameGray,element1);
        Imgproc.Canny(mIntermediateMat, mIntermediateMat, 30, 90);
        //Imgproc.threshold(mIntermediateMat,mIntermediateMat,40,170,Imgproc.THRESH_BINARY);
        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);

        ArrayList<MatOfPoint> filteredContours = new ArrayList<>();

        if(contours.size()>0){
            //System.out.println(hierarchy.size().width + ":hierachy --- contoursize" + contours.size());
            /*filteredContours.add(contours.get(0));
            for (int i = 0; i<hierarchy.size().width;i++) {
                double nextContourSameLevel = hierarchy.get(0,i)[0];
                double parentContour = hierarchy.get(0,i)[3];
                if(nextContourSameLevel != -1 && parentContour != 3){
                    filteredContours.add(contours.get((int)nextContourSameLevel));
                }
            }*/
            //ArrayList<MatOfPoint> parents = new ArrayList<>();
            /*for(int i=0; i<hierarchy.size().width;i++){
                double parent = hierarchy.get(0,i)[2];
                double child = hierarchy.get(0,i)[3];
                if(parent == -1 && child == -1){
                    filteredContours.add(contours.get(i));
                }
                if(parent != -1 && child == -1){
                    filteredContours.add(contours.get(i));
                }
            }*/

            //Only use the inner contours
            for(int i=0; i<hierarchy.size().width;i++){
                double nextChild = hierarchy.get(0,i)[2];
                double indexParent = hierarchy.get(0,i)[3];
                if(nextChild == -1 && indexParent != -1){
                    filteredContours.add(contours.get(i));
                    temp.add(hierarchy.get(0,i));
                }
            }
        }


        if(filteredContours != null) {
            filteredContours = getContoursLargerThan(filteredContours, 3000);
            /*if(contours!=null)
            for (MatOfPoint temp: contours) {
                Rect rect = Imgproc.boundingRect(temp);
                Imgproc.rectangle(frameGray,new Point(rect.x,rect.y),new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,255,255));
            }*/
            //if(contours != null)
            //System.out.println("----------------------PARENT COUNT" + contours.size() +" Before Filter " + contours.size());
        }
        mIntermediateMat.release();
        return filteredContours;
    }

    public static boolean isContourSquare(MatOfPoint thisContour) {

        Rect ret = null;

        MatOfPoint2f thisContour2f = new MatOfPoint2f();
        MatOfPoint approxContour = new MatOfPoint();
        MatOfPoint2f approxContour2f = new MatOfPoint2f();

        thisContour.convertTo(thisContour2f, CvType.CV_32FC2);

        Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);

        approxContour2f.convertTo(approxContour, CvType.CV_32S);

        if (approxContour.size().height == 4) {
            ret = Imgproc.boundingRect(approxContour);
        }

        return (ret != null);
    }

    /**
     * Methode zur Löschung von Konturen die andere Umschließen
     * @param contours
     * @return
     */
    private static ArrayList<MatOfPoint> checkContours(ArrayList<MatOfPoint> contours  ){
        for(int i=0; i<contours.size();i++){
            MatOfPoint2f contour2f = new MatOfPoint2f();
            contours.get(i).convertTo(contour2f, CvType.CV_32FC2);
            for(int j=1; j<contours.size();j++){
                if(i!=j) {
                    int counter = 0;
                    List<Point> pointList = contours.get(j).toList();

                    for (Point current : pointList) {
                        double val = Imgproc.pointPolygonTest(contour2f, current, false);
                        if (val > 0) {
                            counter++;
                        }
                        if (counter >= 50) {
                            System.out.println("contour gelöscht!!!!!!!!!!!");
                            contours.remove(i);
                            if (i > 0) {
                                i = i - 1;
                            }
                            break;
                        }
                    }
                    if (counter > 50) {
                        break;
                    }
                }
            }
        }
        return contours;
    }
    



    /**
     * Draw a grid on frame
     * @param image frame
     * @param gridRows grid row/ column count
     */
    public static void drawGridOnMat(Mat image, int gridRows){
        Scalar gridColor = new Scalar(0,0,0);
        for(int i=0; i<=gridRows ; i++){
            Imgproc.line(image,new Point(45,i*55 + 45),new Point(image.size().height-45,i*55 + 45),gridColor);
            Imgproc.line(image,new Point(i*55 + 45, 45),new Point(i*55 + 45,image.size().height-45),gridColor);
        }
        //Imgproc.line(image,new Point(0,0),new Point(1,1),gridColor);
    }


    /**
     * Draw contours on frame
     * @param image frame
     * @param contours contours to be drawn on frame
     */
    public static void drawContours(Mat image, ArrayList<MatOfPoint> contours){
        //ArrayList<MatOfPoint> contourList = getContoursLargerThan(contours, 1000);
        //Random random = new Random();

        if(contours != null && contours.size()>0) {
            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
                Imgproc.drawContours(image, contours, contourIdx, new Scalar(255,0,0),3);//random.nextInt(256), random.nextInt(256), random.nextInt(256)),3);
            }
            /*for (MatOfPoint temp : contours) {
                Rect rect = Imgproc.boundingRect(temp);
                Imgproc.rectangle(image,new Point(rect.x,rect.y),new Point(rect.x+rect.width, rect.y + rect.height),new Scalar(0,255,0),2);
            }*/
        }
    }

    /**
     * Remove contours that are smaller than size
     * @param contours list of contours
     * @param size minimum required size
     * @return
     */
    private static ArrayList<MatOfPoint> getContoursLargerThan(ArrayList<MatOfPoint> contours, double size) {
        if (contours == null || contours.size() == 0) {
            return null; // No contours found
        }
        ArrayList<MatOfPoint> returnList = new ArrayList<>();

        // Find Contours larger than -size-
        for(MatOfPoint contour : contours){
            if(Imgproc.contourArea(contour)>size){
                //System.out.println(Imgproc.contourArea(contour));
                returnList.add(contour);
            }
        }
        return returnList;
    }

    /**
     * Set image and compute contours
     * Saves frames with contours temporary
     * @param inputFrame frame
     * @return contours found in image
     */
    public static ArrayList<MatOfPoint> setImage(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        //frameGrayList.add(inputFrame.gray());
        //frameRGBAList.add(inputFrame.rgba());
        //inputFrame.gray().copyTo(frameGrayList.get(frameGrayList.size()));
        //inputFrame.rgba().copyTo(frameRGBAList.get(frameRGBAList.size()));
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        contours = blurAndCalculateContours(inputFrame.gray());

        if(contours != null && contours.size()>0){
            //System.out.println("Add New Frame -- Current FrameCount:" + lastFrames.size() + " -- ");
            ImageBuffer currentImage = new ImageBuffer(inputFrame,contours);
            lastFrames.add(currentImage);
            if(lastFrames.size() > 6){
                lastFrames.get(0).reset();
                lastFrames.remove(0);
            }
        }
        return contours;
    }



    /**
     * Get contours of saved frame
     * @return
     */
    public static ArrayList<MatOfPoint> getContoursOfFrame(){
        return lastFrames.get(getImageBufferId()).getContour();
    }

    /**
     * Get id of frame which probably contains the best result
     * @return
     */
    private static int getImageBufferId(){
        int contourSize =0;
        int id =0;
        for(int i=0;i<lastFrames.size();i++){
           if(lastFrames.get(i).getContourSize() >= contourSize){
               contourSize=lastFrames.get(i).getContourSize();
               id=i;
           }
        }
        /*for (ImageBuffer image:lastFrames) {
            if(image.getContourSize() > contourSize){
                id = lastFrames.indexOf(image);
            }
        }*/
        return id;
    }

    /**
     * Get saved GRAY frame which has the most contours
     * @return GRAY frame
     */
    //public static Mat getImageGRAY(){
       /* int contourSize =0;
        Mat rgb = new Mat();
        for (ImageBuffer image:lastFrames) {
            if(image.getContourSize() > contourSize){
                rgb = image.getImageGRAY();
            }
        }
        return rgb;
        */
      //  return lastFrames.get(getImageBufferId()).getImageGRAY();
    //}

    /**
     * Get saved RGBA frame which has the most contours
     * @return RGBA frame
     */
    public static Mat getImageRGB(){
        /*int contourSize =0;
        Mat rgb = new Mat();
        for (ImageBuffer image:lastFrames) {
            if(image.getContourSize() > contourSize){
                rgb = image.getImageRGBA();
            }
        }
        return rgb;*/
        return lastFrames.get(getImageBufferId()).getImageRGBA();
    }
}
