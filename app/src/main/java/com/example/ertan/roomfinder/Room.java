package com.example.ertan.roomfinder;


import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Class to save and generate a Room for the simulation
 */
public class Room {
    private final double blockSize = 55;

    private boolean[][] roomBlock = new boolean[18][18];
    private String transferString = "";
    private int x;
    private int y;
    private int lengthX;
    private int lengthY;

    /**
     * Constructor to create a Room and approximate size
     * @param contour
     */
    public Room(MatOfPoint contour){
        Rect rect = Imgproc.boundingRect(contour);
        double tempX = (rect.x-45) / blockSize;
        this.x = (int)Math.round(tempX);
        double tempY = (rect.y-45) / blockSize;
        this.y = (int) Math.round(tempY);

        this.lengthX = (int) Math.round(rect.width/blockSize);
        this.lengthY = (int) Math.round(rect.height/blockSize);
        setRectInArray();
        calcDifferenceContourRect(contour);
        calcTransferString();
    }

    public boolean isLargeEnough(){
        return (this.lengthX>0 && this.lengthY >0);
    }

    /**
     * Calculate the differences between the bounding rect and the actual contour
     * Removes squares from room if they aren't in the contour
     * @param contour
     */
    private void calcDifferenceContourRect(MatOfPoint contour){
        MatOfPoint2f contour2f = new MatOfPoint2f();
        contour.convertTo(contour2f, CvType.CV_32FC2);

        for(int y=0; y<18;y++) {
            for(int x=0;x<18;x++){
                if(roomBlock[x][y]){
                    double[] points = new double[5];

                    points[0] = Imgproc.pointPolygonTest(contour2f,new Point(x*blockSize + blockSize/3 +45 , y*blockSize + blockSize/3 +45),true);
                    points[1] = Imgproc.pointPolygonTest(contour2f,new Point(x*blockSize + 2*blockSize/3 +45 , y*blockSize + blockSize/3 +45),true);
                    points[2] = Imgproc.pointPolygonTest(contour2f,new Point(x*blockSize + blockSize/3 +45 , y*blockSize + 2*blockSize/3 +45),true);
                    points[3] = Imgproc.pointPolygonTest(contour2f,new Point(x*blockSize + 2*blockSize/3 +45 , y*blockSize + 2*blockSize/3 +45),true);
                    points[4] = Imgproc.pointPolygonTest(contour2f,new Point(x*blockSize + blockSize/2 +45 , y*blockSize + blockSize/2 +45),true);


                    int counterPointInContour = 0;
                    for(int l=0;l<points.length;l++){
                        if(points[l]>=0){
                            counterPointInContour++;
                        }
                    }
                    if(counterPointInContour < 3){
                        roomBlock[x][y] = false;
                    }
                }
            }
        }
    }



    /**
     * Calculate roomsquares out of size and position
     */
    public void setRectInArray(){
        for(int i=y-1; i< lengthY+y+2;i++){
            for(int j=x-1; j<lengthX+x+2;j++){
                if(j<18 && i<18 && j >=0 && i >=0) {
                    roomBlock[j][i] = true;
                }
            }
        }
    }

    /**
     * Generate string which can be transferred to unity
     */
    private void calcTransferString(){
        transferString = "";
        for (boolean[] temp: roomBlock) {
            for(boolean element : temp){
                transferString += element+";";
            }

        }
    }

    /**
     * Getter for transfer string
     * @return
     */
    public String getTransferString() {
        return transferString;
    }
}
