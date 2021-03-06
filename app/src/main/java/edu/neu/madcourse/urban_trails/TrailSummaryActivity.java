package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;
import edu.neu.madcourse.urban_trails.models.User;

public class TrailSummaryActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Trail trail;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.handler = new Handler();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_trail_summary);
        this.trail = (Trail) getIntent().getBundleExtra("bundle").getSerializable("trail");
        Log.v("TESTMD", this.trail.getTrailImageFilename());
        Utils.displayThumbnail(this, (ImageView) findViewById(R.id.imageView3), this.trail.getTrailImageFilename(), null);
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

                new SaveTrailInFirebase(getApplicationContext(), this.databaseReference, this.handler).saveTrail(this.trail);
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            
                break;
        }
    }
}

class SaveTrailInFirebase implements Runnable {

    private final DatabaseReference databaseReference;
    private final Context context;
    private final Handler handler;

    SaveTrailInFirebase(Context context, DatabaseReference databaseReference, Handler handler) {
        this.databaseReference = databaseReference;
        this.context = context;
        this.handler = handler;
    }

    public void saveTrail(final Trail trail) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String username = currentUser.getDisplayName();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images").child(username);
            for (Stop stop : trail.getStops()) {
                if (stop.getImageFileName() != null) {
                    Uri uri = Uri.fromFile(Utils.getLocalImageFile(context, stop.getImageFileName()));
                    imagesRef.child(uri.getLastPathSegment()).putFile(uri);
                }
            }
            if (trail.getTrailImageFilename() != null) {
                Uri uri = Uri.fromFile(Utils.getLocalImageFile(context, trail.getTrailImageFilename()));
                imagesRef.child(uri.getLastPathSegment()).putFile(uri);
            }

            final DatabaseReference myUserReference = this.databaseReference.child("users").child(username);
            myUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.v("TESTMDBT", snapshot.toString());

                    final User myUser = snapshot.getValue(User.class);

                    List<Trail> trails = myUser.getTrails();
                    if (trails == null) {
                        trails = new ArrayList<>();
                    }
                    trails.add(trail);
                    myUser.setTrails(trails);

                    final List<Trail> finalTrails = trails;
                    myUserReference.setValue(myUser, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Log.v("TESTMDBT error: ", "null");
                            } else {
                                Log.v("TESTMDBT error: ", error.toString());
                            }
                            Log.v("TESTMDBT ref: ", ref.toString());
                            myUserReference.child("trails").child(Integer.toString(finalTrails.size() - 1)).child("timestamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    Toast.makeText(context, "Saved trail successfully!", Toast.LENGTH_LONG).show();
                                    
                                    new Utils.MessageSender(databaseReference, context, handler).notifyFriendsOfNewTrail(myUser);
                                    
                                }
                            });
                        }
                    });
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