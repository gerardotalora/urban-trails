package edu.neu.madcourse.urban_trails;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Utils.getStorageDir(context);

        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private static File getStorageDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    /**
     *
     * @param imageView
     * @param filename should be relative to the storageDir
     */
    public static void displayThumbnail(Context context, ImageView imageView, String filename) {
        if (Utils.getLocalImageFile(context, filename).isFile()) {
            Utils.displayThumbnailFromLocal(context, imageView, filename);
        } else {
            Utils.displayThumbnailFromCloudStorage(context, imageView, filename);
        }
    }

    private static void displayThumbnailFromLocal(Context context, ImageView imageView, String filename) {
        filename = Utils.getLocalFilepath(context, filename).toString();
        final int THUMBSIZE = 1000;
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filename),
                THUMBSIZE, THUMBSIZE);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filename);
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

    private static Path getLocalFilepath(Context context, String filename) {
        return Paths.get(Utils.getStorageDir(context).getAbsolutePath(), filename);
    }

    private static void displayThumbnailFromCloudStorage(final Context context, final ImageView imageView, String filename) {
        Utils.getImageUrl(filename).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static double feetToDegrees(double feet) {
        return feet / (70 * 5280);
    }

    public static File getLocalImageFile(Context context, String imageFileName) {
        return Utils.getLocalFilepath(context, imageFileName).toFile();
    }

    public static Task<Uri> getImageUrl(String trailImageFilename) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("images").child(trailImageFilename);
            return imageReference.getDownloadUrl();
        } else {
            return null;
        }
    }
}
