package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import edu.neu.madcourse.urban_trails.models.User;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText username;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText phoneNumber;
    private Handler handler = new Handler();
    private final String TAG = "MDSignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        username = findViewById(R.id.textUsername);
        firstName = findViewById(R.id.textFirstName);
        lastName = findViewById(R.id.textLastName);
        email = findViewById(R.id.textEmail);
        password = findViewById(R.id.textPassword);
        phoneNumber = findViewById(R.id.textPhone);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(getApplicationContext(), "User is still signed in", Toast.LENGTH_LONG).show();
            Log.v(TAG,"user already signed in with auth user");
            String name = currentUser.getDisplayName();
            Log.v(TAG,name);
            GetUser getUser = new GetUser();
            getUser.getUser(name);
        }
    }

    public void onClick(View view) {

        SignUpUser signUpUser = new SignUpUser();

        final String usernameString = username.getText().toString();
        final String firstNameString = firstName.getText().toString();
        final String lastNameString = lastName.getText().toString();
        final String emailString = email.getText().toString();
        final String passwordString = password.getText().toString();
        final String phoneNumberString = phoneNumber.getText().toString();

        if (!firstNameString.matches("^[a-zA-Z0-9]*$") || firstNameString.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter an alphanumeric firstName", Toast.LENGTH_LONG).show();
        } else if (!lastNameString.matches("^[a-zA-Z0-9]*$") || lastNameString.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter an alphanumeric lastName", Toast.LENGTH_LONG).show();
        } else if (emailString.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter an email", Toast.LENGTH_LONG).show();
        } else if (passwordString.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();
        } else if (phoneNumberString.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter a phone number", Toast.LENGTH_LONG).show();
        } else if (!usernameString.matches("^[a-zA-Z0-9]*$") || usernameString.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter an alphanumeric username", Toast.LENGTH_LONG).show();
        } else {
            signUpUser.signUpUser(usernameString, firstNameString, lastNameString, emailString, passwordString, phoneNumberString);
        }
    }

    private class SignUpUser implements Runnable {
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String phoneNumber;
        private User user;

        SignUpUser() {
        }

        public void signUpUser(String username, String firstName, String lastName, String email, String password, String phoneNumber) {
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.phoneNumber = phoneNumber;
            this.runInNewThread();
        }

        @Override
        public void run() {

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });

                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<InstanceIdResult>() {
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
                                                } else {
                                                    user = new User(username, firstName, lastName, email, password, phoneNumber, token);
                                                    databaseReference.child("users").child(user.getUsername()).setValue(user);
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
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        }

        public void runInNewThread() {
            new Thread(this).start();
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
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<InstanceIdResult>() {
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
        Intent intentHome = new Intent(SignUpActivity.this, HomeActivity.class);
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