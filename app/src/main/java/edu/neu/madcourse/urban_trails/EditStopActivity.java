package edu.neu.madcourse.urban_trails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

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
                    photoFile = Utils.createImageFile(this);
                    this.stop.setImageFileName(Uri.fromFile(photoFile).getPath());
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
            this.displayImageForStop();
        }
    }

    private void displayImageForStop() {
        ImageView imageView = findViewById(R.id.imageView3);
        Utils.displayThumbnail(imageView, this.stop.getImageFileName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("stop", this.stop);
    }

}