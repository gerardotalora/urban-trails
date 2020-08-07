package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.type.Color;

import edu.neu.madcourse.urban_trails.models.User;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Handler handler = new Handler();
    private final String TAG = "MainActivity";
    private DatabaseReference databaseReference;
    private EditText email;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        email = findViewById(R.id.textLoginEmail);
        password = findViewById(R.id.textLoginPassword);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(getApplicationContext(), "User logged in", Toast.LENGTH_LONG).show();
            Log.v(TAG, "user already signed in with auth user");
            String name = currentUser.getDisplayName();
            Log.v(TAG, name);
            GetUser getUser = new GetUser();
            getUser.getUser(name);
        }
    }

    public void onClickLogin(View view) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(getApplicationContext(), "User logged in", Toast.LENGTH_LONG).show();
            Log.v(TAG,"user already signed in with auth user");
            String name = currentUser.getDisplayName();
            Log.v(TAG,name);
            GetUser getUser = new GetUser();
            getUser.getUser(name);
        } else {
            final String emailString = email.getText().toString();
            final String passwordString = password.getText().toString();

            if (emailString.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter an email", Toast.LENGTH_LONG).show();
            } else if (passwordString.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();
            } else {
                firebaseAuth.signInWithEmailAndPassword(emailString, passwordString)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    String name = user.getDisplayName();
                                    Log.v(TAG,name);
                                    GetUser getUser = new GetUser();
                                    getUser.getUser(name);
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    public void onClickSignUp(View view) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(getApplicationContext(), "User logged in", Toast.LENGTH_LONG).show();
            Log.v(TAG,"user already signed in with auth user");
            String name = currentUser.getDisplayName();
            Log.v(TAG,name);
            GetUser getUser = new GetUser();
            getUser.getUser(name);
        } else {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        }
    }

    private class GetUser implements Runnable {

        private String username;
        private User user;

        public GetUser() {
        }

        public void getUser(String username) {
            this.username = username;
            this.runInNewThread();
        }

        @Override
        public void run() {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(final InstanceIdResult instanceIdResult) {
                    final String token = instanceIdResult.getToken();
                    Log.v(TAG,token);
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(username)) {
                                user = snapshot.child(username).getValue(User.class);

                                if (!user.getToken().equals(token)) {
                                    user.setToken(token);
                                    databaseReference.child("users").child(user.getUsername()).setValue(user);
                                }
                                startIntent(user);
                            } else {
                                showToast("User exists in Auth but not in the Database");
                            }
                            startIntent(user);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showToast("Error connecting to database");
                        }
                    });
                }
            });
        }

        public void runInNewThread() {
            new Thread(this).start();
        }
    }

    private void startIntent(User user) {
        Intent intentHome = new Intent(MainActivity.this, HomeActivity.class);
        intentHome.putExtra("USER_USERNAME", user.getUsername());
        intentHome.putExtra("USER_FIRST_NAME", user.getFirstName());
        intentHome.putExtra("USER_LAST_NAME", user.getLastName());
        intentHome.putExtra("USER_EMAIL", user.getEmail());
        intentHome.putExtra("USER_PASSWORD", user.getPassword());
        intentHome.putExtra("USER_PHONE_NUMBER", user.getPhoneNumber());
        intentHome.putExtra("USER_TOKEN", user.getToken());
        startActivity(intentHome);
    }

    private void showToast(final String toastText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
            }
        });
    }
}