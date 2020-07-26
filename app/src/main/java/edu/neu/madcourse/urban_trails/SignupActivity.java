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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import edu.neu.madcourse.urban_trails.models.User;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText phoneNumber;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firstName = findViewById(R.id.textFirstName);
        lastName = findViewById(R.id.textLastName);
        email = findViewById(R.id.textEmail);
        password = findViewById(R.id.textPassword);
        phoneNumber = findViewById(R.id.textPhone);

    }

    public void onClick(View view) {

        SignUpUser signUpUser = new SignUpUser();

        final String firstNameString = firstName.getText().toString();
        final String lastNameString = lastName.getText().toString();
        final String emailString = email.getText().toString();
        final String passwordString = password.getText().toString();
        final String phoneNumberString = phoneNumber.getText().toString();

        signUpUser.signUpUser(firstNameString, lastNameString, emailString, passwordString, phoneNumberString);

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
        } else {
            signUpUser.signUpUser(firstNameString, lastNameString, emailString, passwordString, phoneNumberString);
        }
    }

    private class SignUpUser implements Runnable {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String phoneNumber;
        private User user;

        SignUpUser() {
        }

        public void signUpUser(String firstName, String lastName, String email, String password, String phoneNumber) {
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
                                task.getResult().getUser();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignupActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(final InstanceIdResult instanceIdResult) {
                                        final String token = instanceIdResult.getToken();
                                        Log.v("Token",token);

                                        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                if (snapshot.hasChild(email)) {
                                                    user = snapshot.child(email).getValue(User.class);

                                                    if (!user.getToken().equals(token)) {
                                                        user.setToken(token);
                                                        databaseReference.child("users").child(user.getEmail()).setValue(user);
                                                    }
                                                } else {
                                                    user = new User(firstName, lastName, email, password, phoneNumber, token);
                                                    databaseReference.child("users").child(user.getEmail()).setValue(user);
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
                        }
                    }
            );
        }

        public void runInNewThread() {
            new Thread(this).start();
        }

        private void showToast(final String toastText) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
                }
            });
        }

        private void startIntent(User user) {
            Intent intentHome = new Intent(SignupActivity.this, HomeActivity.class);
            intentHome.putExtra("USER_FIRST_NAME", user.getFirstName());
            intentHome.putExtra("USER_LAST_NAME", user.getLastName());
            intentHome.putExtra("USER_EMAIL", user.getEmail());
            intentHome.putExtra("USER_PASSWORD", user.getPassword());
            intentHome.putExtra("USER_PHONE_NUMBER", user.getPhoneNumber());
            intentHome.putExtra("USER_TOKEN", user.getToken());
            startActivity(intentHome);
        }
    }
}