package io.ashik619.edgetensortest;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {
    private static final String TAG = "CameraActivity";
    static {
        if(!OpenCVLoader.initDebug()){
             Log.e(TAG, "OpenCV not loaded");
        } else {
             Log.e(TAG, "OpenCV loaded");
        }
    }

    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;

    FrameLayout cameraContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int camId = getIntent().getIntExtra("cam",99);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraContent = new FrameLayout(this);
        Button closeButton = new Button(this);
        closeButton.setText("Close");
        closeButton.setPadding(10,10,10,10);
        closeButton.setBackgroundResource(R.drawable.buton_bg);
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        FrameLayout.LayoutParams cameraViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        closeButton.setLayoutParams(layoutParams);
        openCvCameraView = new JavaCameraView(CameraActivity.this, camId);
        openCvCameraView.setCvCameraViewListener(CameraActivity.this);
        openCvCameraView.setLayoutParams(cameraViewParams);
        if(closeButton.getParent()!=null)
            ((ViewGroup)closeButton.getParent()).removeView(closeButton);
        if(openCvCameraView.getParent()!=null)
            ((ViewGroup)openCvCameraView.getParent()).removeView(openCvCameraView);
        cameraContent.addView(openCvCameraView);
        cameraContent.addView(closeButton);
        switch (getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(cameraContent);
                break;

            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.change_orientation_layout);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
       // Log.e("CONFIG",newConfig.orientation+"");
        switch (newConfig.orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(cameraContent);
                break;

            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.change_orientation_layout);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeOpenCVDependencies();
    }

    private void initializeOpenCVDependencies(){
        try{
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_APPEND);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }

        // And we are ready to go
        openCvCameraView.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat aInputFrame) {
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);

        MatOfRect faces = new MatOfRect();

        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i <facesArray.length; i++)
            Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return aInputFrame;
    }
}
