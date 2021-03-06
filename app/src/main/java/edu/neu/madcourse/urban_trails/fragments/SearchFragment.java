package edu.neu.madcourse.urban_trails.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.urban_trails.NavigationFragment;
import edu.neu.madcourse.urban_trails.R;
import edu.neu.madcourse.urban_trails.Utils;
import edu.neu.madcourse.urban_trails.models.User;

public class SearchFragment extends Fragment implements NavigationFragment {

    private final String TAG = "Search Fragment";

    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    ArrayList<TaggedUser> users = new ArrayList<>();
    ArrayAdapter<TaggedUser> adapter;
    private ListView usersListView;
    private View view;

    public SearchFragment() {
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);

        usersListView = view.findViewById(R.id.users_list);

        adapter = new UserAdapter(view.getContext(), users);
        usersListView.setAdapter(adapter);

        UsersRunnable usersRunnable = new UsersRunnable();
        usersRunnable.getUsers();


        return view;
    }

    private class TaggedUser {
        private final User user;

        public User getUser() {
            return user;
        }

        public boolean isFriend() {
            return isFriend;
        }

        public void setFriend(boolean friend) {
            isFriend = friend;
        }

        private boolean isFriend;

        public TaggedUser(User user) {
            this.user = user;
            this.isFriend = false;
        }
    }

    @Override
    public int getTitle() {
        return R.string.search_for_friends;
    }

    private class UsersRunnable implements Runnable {

        private User user;

        UsersRunnable() {
        }

        public void getUsers() {
            this.runInNewThread();
        }

        @Override
        public void run() {
            databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    User myUser = null;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.i(TAG, dataSnapshot.toString());
                        User user = dataSnapshot.getValue(User.class);
                        if (!user.getUsername().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                            TaggedUser taggedUser = new TaggedUser(user);
                            users.add(taggedUser);
                        } else {
                            myUser = user;
                        }
                    }
                    if (myUser != null) {
                        for (TaggedUser taggedUser : users) {
                            if (myUser.getFriends() != null && myUser.getFriends().contains(taggedUser.getUser().getUsername())) {
                                taggedUser.setFriend(true);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
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

    private class UserAdapter extends ArrayAdapter<TaggedUser> {

        private Context context;
        private List<TaggedUser> users;

        public UserAdapter(Context context, List<TaggedUser> stickers) {
            super(context, -1, stickers);
            this.context = context;
            this.users = stickers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View userView = inflater.inflate(R.layout.user_card, parent, false);
            TextView textView = (TextView) userView.findViewById(R.id.user_name);
            TextView textViewDesc = (TextView) userView.findViewById(R.id.user_description);
            ImageView imageView = (ImageView) userView.findViewById(R.id.user_image);

            final int friendPostion = position;
            final Button addButton = userView.findViewById(R.id.add_button);
            final TextView existingFriendTextView = userView.findViewById(R.id.existingFriendTextView);

            if (users.get(position).isFriend()) {
                addButton.setVisibility(View.INVISIBLE);
                existingFriendTextView.setVisibility(View.VISIBLE);
            } else {
                addButton.setVisibility(View.VISIBLE);
                existingFriendTextView.setVisibility(View.GONE);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            final String name = currentUser.getDisplayName();
                            Log.i(TAG, "adding user");
                            AddFriend addFriend = new AddFriend();
                            addFriend.addFriend(name, users.get(friendPostion).getUser().getUsername());
                            addButton.setVisibility(View.INVISIBLE);
                            existingFriendTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            if (users.get(position).getUser().getImageFileName() != null) {
                displayImageForProfile(users.get(position).getUser(), imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_baseline_person_24);
            }
            String text = users.get(position).getUser().getUsername();
            String userDesc = users.get(position).getUser().getFirstName() + "\n" + users.get(position).getUser().getLastName();
            textView.setText(text);
            textViewDesc.setText(userDesc);
            Log.i(TAG, users.get(position).getUser().getUsername());
            return userView;

        }

        private void displayImageForProfile(User user, ImageView imageView) {
            if (user.getImageFileName() != null) {
                Utils.displayThumbnail(view.getContext(), imageView, user.getImageFileName(), null);
            }
        }
    }

    private class AddFriend implements Runnable {

        private String username;
        private String friend;

        AddFriend() {
        }

        public void addFriend(String username, String friend) {
            this.username = username;
            this.friend = friend;
            this.runInNewThread();
        }

        @Override
        public void run() {
            databaseReference.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    List<String> friends = user.getFriends();
                    if (friends == null) {
                        friends = new ArrayList<>();
                    }

                    if (friends.contains(friend) || username.equals(friend)) {
                        Log.v(TAG, friends.toString());
                        Log.v(TAG, friend);
                    } else {
                        friends.add(friend);
                        showToast("Added New Friend: " + friend);
                    }

                    user.setFriends(friends);

                    final List<String> finalFriends = friends;
                    databaseReference.child("users").child(username).setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Log.v("TESTMDBT error: ", "null");
                            } else {
                                Log.v("TESTMDBT error: ", error.toString());
                            }
                            Log.v("TESTMDBT ref: ", ref.toString());
                        }
                    });

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

}

