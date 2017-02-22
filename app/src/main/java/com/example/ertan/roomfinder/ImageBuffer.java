package com.example.ertan.roomfinder;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.ArrayList;

/**
 * Class to temporary save frames
 */
public class ImageBuffer {
    private Mat rgba = new Mat();
    private ArrayList<MatOfPoint> contour = new ArrayList<>();

    public ImageBuffer(CameraBridgeViewBase.CvCameraViewFrame image, ArrayList<MatOfPoint> contour){
        this.contour = contour;
        image.rgba().copyTo(rgba);
    }

    public Mat getImageRGBA() {
        return rgba;
    }

    public ArrayList<MatOfPoint> getContour() {
        return contour;
    }

    public int getContourSize() {
        return contour.size();
    }
    public void reset(){
        rgba.release();
    }
}
