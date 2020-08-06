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
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import edu.neu.madcourse.urban_trails.models.User;

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


    public static class MessageSender implements Runnable {
        private final Context context;
        private final Handler handler;
        private DatabaseReference databaseReference;
        private User myUser;

        // Please add the server key from your firebase console in the following format "key=<serverKey>"
        private static final String SERVER_KEY = "key=AAAAdw6WwLM:APA91bF9WPHbkhWJAeM0c3J68eQ1XBlG2-DmvEkln41DEsrb77uN3bK1v61PiVVAkGvATxt_WxURPLqKiReGsvoIxGg2th8WPDk_y46Y8kl-F1iOe9I5x0D4f88n_JseWsraJiLmMait";


        MessageSender(DatabaseReference databaseReference, Context context, Handler handler) {
            this.databaseReference = databaseReference;
            this.context = context;
            this.handler = handler;
        }

        public void notifyFriendsOfNewTrail(User myUser) {
            this.myUser = myUser;
            this.runInNewThread();
        }

        @Override
        public void run() {
            databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    final List<User> recipientUsers = new ArrayList<>();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        User otherUser = childSnapshot.getValue(User.class);
                        if (otherUser != null && otherUser.getFriends() != null && otherUser.getFriends().contains(myUser.getUsername())) {
                            recipientUsers.add(otherUser);
                        }
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageToDevice(myUser, recipientUsers);
                        }
                    }).start();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Error connecting to database");
                }
            });
        }

        public void runInNewThread() {
            new Thread(this).start();
        }

        private void showToast(final String toastText) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
                }
            });
        }

        /**
         * Pushes a notification to a given device-- in particular, this device,
         * because that's what the instanceID token is defined to be.
         */
        private void sendMessageToDevice(User sender, List<User> recipients) {
            try {
                JSONObject jPayload = createPayload(sender, recipients);
                HttpURLConnection conn = getHttpURLConnection();
                sendPayload(conn, jPayload);
                readFCMResponse(conn);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        private JSONObject createPayload(User sender, List<User> recipients) throws JSONException {
            JSONObject jPayload = new JSONObject();
            JSONObject jNotification = new JSONObject();
            JSONObject jdata = new JSONObject();
            jNotification.put("title", sender.getFirstName() + " just made a new trail!");
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");

            JSONArray ja = new JSONArray();
            for (User recipientUser : recipients) {
                ja.put(recipientUser.getToken());
            }

            jPayload.put("registration_ids", ja);
            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
            jPayload.put("data", jdata);

            return jPayload;
        }

        private HttpURLConnection getHttpURLConnection() throws IOException {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            return conn;
        }

        private void readFCMResponse(HttpURLConnection conn) throws IOException {
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);
            Log.v("fcmresponse", resp);
        }

        private void sendPayload(HttpURLConnection conn, JSONObject jPayload) throws IOException {
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());
            outputStream.close();
        }

        /**
         * Helper function
         *
         * @param is
         * @return
         */
        private static String convertStreamToString(InputStream is) {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next().replace(",", ",\n") : "";
        }
    }
}
