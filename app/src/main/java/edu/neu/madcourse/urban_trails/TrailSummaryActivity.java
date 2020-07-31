package edu.neu.madcourse.urban_trails;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.InputStream;

public class TrailSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_summary);

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();

            ImageView tv1;
            tv1= findViewById(R.id.imageView3);
            tv1.setImageBitmap(bmp);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}