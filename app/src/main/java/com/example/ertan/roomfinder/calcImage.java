package com.example.ertan.roomfinder;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.ArrayList;

public class calcImage extends AppCompatActivity {



    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc_image);

        imageView = (ImageView) findViewById(R.id.imageView);
        //Button btnCalculate = (Button) findViewById(R.id.btnNewCalculation);
        //btnCalculate.setOnClickListener(onClickListener());
        Button btnSendData = (Button) findViewById(R.id.btnSendData);
        btnSendData.setOnClickListener(onClickListenerSendData());

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat frameRGB = new Mat();
        //Mat frameGray = new Mat();
        UtilRoomCalculator.getImageRGB().copyTo(frameRGB);
        //UtilRoomCalculator.getImageGRAY().copyTo(frameGray);

        contours = UtilRoomCalculator.getContoursOfFrame();
        UtilRoomCalculator.drawContours(frameRGB,contours);

        UtilRoomCalculator.drawGridOnMat(frameRGB,18);
        showMatOnScreen(frameRGB);


/*
        Bitmap imageBitmap = Bitmap.createBitmap(1080,1080,Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(currentImage,imageBitmap);
        imageView.setImageBitmap(imageBitmap);
*/
    }



    /**
     * Create OnClickListener for compute button
     * @return OnClickListener
     */
    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<MatOfPoint> contours = new ArrayList<>();
                Mat frameRGB = new Mat();
                //Mat frameGray = new Mat();
                UtilRoomCalculator.getImageRGB().copyTo(frameRGB);
                //UtilRoomCalculator.getImageGRAY().copyTo(frameGray);

                contours = UtilRoomCalculator.getContoursOfFrame();
                UtilRoomCalculator.drawContours(frameRGB,contours);

                UtilRoomCalculator.drawGridOnMat(frameRGB,18);
                showMatOnScreen(frameRGB);
            }
        };
    }

    /**
     * Create OnClickListener for transfer button
     * @return OnClickListener
     */
    private View.OnClickListener onClickListenerSendData(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Mat frameGray = new Mat();
                ArrayList<MatOfPoint> contours = new ArrayList<>();
                //UtilRoomCalculator.getImageGRAY().copyTo(frameGray);
                contours = UtilRoomCalculator.getContoursOfFrame();
                //contours = UtilRoomCalculator.getContoursLargerThan(contours,1000);

                ArrayList<Room> rooms = new ArrayList<>();
                String transferString = "";
                for (MatOfPoint contour : contours) {

                    Room currentRoom = new Room(contour);
                    boolean isUnique = true;
                    if(rooms != null){

                        for (Room r: rooms) {
                            if(r.getTransferString().equals(currentRoom.getTransferString())){
                                isUnique = false;
                            }
                        }
                    }
                    if(isUnique) {
                        rooms.add(currentRoom);
                    }

                }
                System.out.print("-------" +contours.size() + " Contoursize - CALC");
                for (Room r : rooms) {
                    transferString += r.getTransferString()+"\n";
                }
                System.out.print("-------" +rooms.size() + " Rooms - CALC");


                RequestHandler rh = new RequestHandler();
                String requestData = rh.transferData(transferString);
                if(!requestData.contains("Erfolgreich")){
                    requestData = "Es ist ein Fehler aufgetreten";
                }
                Toast toast = Toast.makeText(getApplicationContext(),requestData,Toast.LENGTH_LONG);
                toast.show();
                System.out.println(requestData);


                //System.out.print("Transferstring ------- " + transferString);
            }
        };
    }

    /**
     * Method to display a Mat object in an imageview
     * @param currentImage
     */
    private void showMatOnScreen(Mat currentImage){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        //UtilRoomCalculator.lastFrameRGB.copyTo(currentImage);
        //Mat currentImage = UtilRoomCalculator.lastFrameRGB;
        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(currentImage.cols(), currentImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(currentImage, bmp);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bmp , 0, 0, bmp .getWidth(), bmp .getHeight(), matrix, true);
        imageView.setImageBitmap(rotatedBitmap);
    }
}
