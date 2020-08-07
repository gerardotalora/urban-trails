package edu.neu.madcourse.urban_trails.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import edu.neu.madcourse.urban_trails.HomeActivity;
import edu.neu.madcourse.urban_trails.R;
import edu.neu.madcourse.urban_trails.Utils;
import edu.neu.madcourse.urban_trails.models.User;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private String userString;
    private TextView name;
    private TextView username;
    private TextView email;
    private ImageView profileImage;
    private View view;
    private User user;
    private Handler handler = new Handler();

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.profile);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userString = firebaseUser.getDisplayName();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        name = view.findViewById(R.id.firstlastname);
        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        profileImage = view.findViewById(R.id.profilepic);

        ImageView editProfilePicButton = view.findViewById(R.id.editProfilePic);
        editProfilePicButton.setOnClickListener(this);

        setProfile();
        return view;
    }

    public void setProfile() {
        databaseReference.child("users").child(userString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);

                String fullName = user.getFirstName() + " " + user.getLastName();
                name.setText(fullName);
                username.setText(user.getUsername());
                email.setText(user.getEmail());
                if (user.getImageFileName() != null) {
                    displayImageForProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error connecting to database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.editProfilePic) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = Utils.createImageFile(v.getContext(), user.getUsername());
                    this.user.setImageFileName(Uri.fromFile(photoFile).getLastPathSegment());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(v.getContext(),
                            "edu.neu.madcourse.urban_trails.file_provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SetUser setUser = new SetUser();
        setUser.setUser(user);

    }

    private class SetUser implements Runnable {

        private User user;

        SetUser() {
        }

        public void setUser(User user) {
            this.user = user;
            this.runInNewThread();
        }

        @Override
        public void run() {

            databaseReference.child("users").child(user.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    user.setImageFileName(Paths.get(user.getUsername(), user.getImageFileName()).toString());
                    databaseReference.child("users").child(user.getUsername()).setValue(user);
                    Uri uri = Uri.fromFile(Utils.getLocalImageFile(view.getContext(), user.getImageFileName()));
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("images").child(user.getUsername());
                    imagesRef.child(uri.getLastPathSegment()).putFile(uri);
                    displayImageForProfile();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Error connecting to database");
                }
            });
        }

        private void showToast(final String toastText) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(view.getContext(), toastText, Toast.LENGTH_LONG).show();
                }
            });
        }

        public void runInNewThread() {
            new Thread(this).start();
        }
    }

    private void displayImageForProfile() {
        if (this.user.getImageFileName() != null) {
            Utils.displayThumbnail(view.getContext(), profileImage, this.user.getImageFileName(), null);
        }
    }
}

