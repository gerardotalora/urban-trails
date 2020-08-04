package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.urban_trails.models.Trail;
import edu.neu.madcourse.urban_trails.models.User;

public class TrailSummaryActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Trail trail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.databaseReference = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_trail_summary);


        this.trail = (Trail) getIntent().getBundleExtra("bundle").getSerializable("trail");

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();

//            ImageView tv1;
//            tv1= findViewById(R.id.imageView3);
//            tv1.setImageBitmap(bmp);

            // Create base64 image
            Bitmap bitmapBase64 = bmp.copy(bmp.getConfig(), true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapBase64.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            bitmapBase64.recycle();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imageB64 = Base64.encodeToString(byteArray, Base64.URL_SAFE);
            this.trail.setTrailImageBase64(imageB64);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveStopButton:
                EditText nameview = findViewById(R.id.stop_name);
                EditText descriptionview = findViewById(R.id.trail_summary_description);

                String name = nameview.getText().toString();
                String description = descriptionview.getText().toString();

                this.trail.setName(name);
                this.trail.setDescription(description);

                new SaveTrailInFirebase(getApplicationContext(), this.databaseReference).saveTrail(this.trail);
                break;
        }
    }
}

class SaveTrailInFirebase implements Runnable {

    private final DatabaseReference databaseReference;
    private final Context context;

    SaveTrailInFirebase(Context context, DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
        this.context = context;
    }

    public void saveTrail(final Trail trail) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String name = currentUser.getDisplayName();
            final DatabaseReference myUserReference = this.databaseReference.child("users").child(name);
            myUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.v("TESTMDBT", snapshot.toString());

                    User user = snapshot.getValue(User.class);

                    List<Trail> trails = user.getTrails();
                    if (trails == null) {
                        trails = new ArrayList<>();
                    }
                    trails.add(trail);
                    user.setTrails(trails);

                    final List<Trail> finalTrails = trails;
                    myUserReference.setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Log.v("TESTMDBT error: ", "null");
                            } else {
                                Log.v("TESTMDBT error: ", error.toString());
                            }
                            Log.v("TESTMDBT ref: ", ref.toString());
                            myUserReference.child("trails").child(Integer.toString(finalTrails.size() - 1)).child("timestamp").setValue(ServerValue.TIMESTAMP);
                        }
                    });
//                    String key = databaseReference.child("users").child(name).child("trails").push().getKey();
//                    databaseReference.child("users").child(name).
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(this.context, "currentUser is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void run() {

    }

    public void runInNewThread() {
        new Thread(this).start();
    }
}