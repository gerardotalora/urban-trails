package edu.neu.madcourse.urban_trails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.neu.madcourse.urban_trails.models.Stop;

public class EditStopActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Stop stop;
    private EditText stopNameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stop);

        if (savedInstanceState == null) {
            this.stop = (Stop) getIntent().getBundleExtra("bundle").getSerializable("stop");
        } else {
            this.stop = (Stop) savedInstanceState.getSerializable("stop");
        }
        if (this.stop.getImageFileName() != null) {
            this.displayImageForStop();
        }

        this.stopNameView = findViewById(R.id.stop_name);
        stopNameView.setText(stop.getTitle());
    }

    public void onClick(View view) {
        if (view.getId() == R.id.saveStopButton) {
            Intent intent = new Intent();
            Bundle b = new Bundle();
            this.stop.setTitle(stopNameView.getText().toString());
            b.putSerializable("stop", this.stop);
            intent.putExtra("bundle", b);
            setResult(RESULT_OK, intent);
            finish();
        } else if (view.getId() == R.id.takePictureButton) {
            // Open camera!

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "edu.neu.madcourse.urban_trails.file_provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");


            this.displayImageForStop();

//            imageView.setImageURI(currentPhotoPath);

        }
    }

    private void displayImageForStop() {
        ImageView imageView = findViewById(R.id.imageView3);
        final int THUMBSIZE = 1000;
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(this.stop.getImageFileName()),
                THUMBSIZE, THUMBSIZE);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(this.stop.getImageFileName());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            }
            thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(thumbnail);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        this.stop.setImageFileName(Uri.fromFile(image).getPath());
        return image;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("stop", this.stop);
    }

}