package com.example.fa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button chose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView2);
        chose = findViewById(R.id.button);

        chose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,"choose image"),121);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == 121){
            imageView.setImageURI(data.getData());

            FirebaseVisionImage image;

            try {

                Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                Bitmap mutableBmp = bmp.copy(Bitmap.Config.ARGB_8888,true);
                final Canvas canvas = new Canvas(mutableBmp);
                image = FirebaseVisionImage.fromFilePath(getApplicationContext(),data.getData());

                FirebaseVisionFaceDetectorOptions options =
                        new FirebaseVisionFaceDetectorOptions.Builder()
                                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                                .build();


                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionFace> faces) {

                                                for(FirebaseVisionFace face : faces){
                                                    Rect bounds = face.getBoundingBox();
                                                    Paint p = new Paint();
                                                    p.setColor(Color.YELLOW);
                                                    p.setStyle(Paint.Style.STROKE);
                                                    canvas.drawRect(bounds,p);
                                                    float roty = face.getHeadEulerAngleY();
                                                    float rotz = face.getHeadEulerAngleZ();


                                                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                                    if(leftEar != null){
                                                        FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                                    }

                                                    List<FirebaseVisionPoint> leftEyeContour =
                                                            face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                                                    List<FirebaseVisionPoint> upperLipBottomContour =
                                                            face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                }

                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}