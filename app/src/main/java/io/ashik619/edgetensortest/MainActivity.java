package io.ashik619.edgetensortest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private Button detectFaces;
    private Button frontCam;
    private Button backCam;
    private LinearLayout selectLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detectFaces = (Button) findViewById(R.id.detectBtn);
        frontCam = (Button) findViewById(R.id.frontCam);
        backCam = (Button) findViewById(R.id.backCam);
        selectLayout = (LinearLayout) findViewById(R.id.selectLayout);
        detectFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!runtimePermissionsRequest()){
                    showCamLayout();
                }
            }
        });
        runtimePermissionsRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectLayout.setVisibility(View.GONE);
        detectFaces.setVisibility(View.VISIBLE);
    }

    private void showCamLayout(){
        detectFaces.setVisibility(View.GONE);
        selectLayout.setVisibility(View.VISIBLE);
        frontCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFaceDetection(98);
            }
        });
        backCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFaceDetection(99);
            }
        });

    }

    private boolean runtimePermissionsRequest() {
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 178);
                return true;
            }return false;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==178){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showCamLayout();
            }
        }
    }

    private void openFaceDetection(int cameraType){
        //cameraType ; 0 = back camera ; 1 = front camera
        Intent intent  = new Intent(MainActivity.this,CameraActivity.class);
        intent.putExtra("cam",cameraType);
        startActivity(intent);
    }
}
