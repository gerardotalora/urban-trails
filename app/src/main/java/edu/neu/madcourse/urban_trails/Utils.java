package edu.neu.madcourse.urban_trails;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
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

    public static void displayThumbnail(Context context, ImageView imageView, String filename, OnImageDrawnListener listener) {
        Utils.displayThumbnail(context, imageView, filename, listener, null, null);
    }

    /**
     * @param imageView
     * @param filename  should be relative to the storageDir
     */
    public static void displayThumbnail(Context context, ImageView imageView, String filename, OnImageDrawnListener listener, Integer imageWidth, Integer imageHeight) {
        if (Utils.getLocalImageFile(context, filename).isFile()) {
            Utils.displayThumbnailFromLocal(context, imageView, filename, imageWidth, imageHeight);
        } else {
            Utils.displayThumbnailFromCloudStorage(context, imageView, filename, listener, imageWidth, imageHeight);
        }
    }

    private static void displayThumbnailFromLocal(Context context, ImageView imageView, String filename, Integer imageWidth, Integer imageHeight) {
        filename = Utils.getLocalFilepath(context, filename).toString();
        final int THUMBSIZE = (imageWidth != null && imageHeight != null) ? Integer.max(imageWidth, imageHeight) : 1000;
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

    private static void displayThumbnailFromCloudStorage(final Context context, final ImageView imageView, String filename, final OnImageDrawnListener listener) {
        displayThumbnailFromCloudStorage(context, imageView, filename, listener, null, null);
    }

    private static void displayThumbnailFromCloudStorage(final Context context, final ImageView imageView, String filename, final OnImageDrawnListener listener, final Integer imageWidth, final Integer imageHeight) {
        Utils.getImageUrl(filename).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                RequestBuilder<Drawable> x = Glide.with(context)
                        .load(uri);

                if (imageWidth != null && imageHeight != null) {
                    x = x.override(imageWidth, imageHeight);
                }

                x.into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (listener != null) {
                            listener.onImageDrawn();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
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
        StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("images").child(trailImageFilename);
        return imageReference.getDownloadUrl();
    }
}
