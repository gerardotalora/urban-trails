package edu.neu.madcourse.urban_trails.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.neu.madcourse.urban_trails.NavigationFragment;
import edu.neu.madcourse.urban_trails.R;
import edu.neu.madcourse.urban_trails.RvAdapter;
import edu.neu.madcourse.urban_trails.TrailDetailActivity;
import edu.neu.madcourse.urban_trails.models.RecyclerTrail;
import edu.neu.madcourse.urban_trails.models.Trail;
import edu.neu.madcourse.urban_trails.models.User;

public class HomeFragment extends Fragment implements NavigationFragment {

    private final String TAG = "Home Fragment";

    private ArrayList<RecyclerTrail> recyclerTrails = new ArrayList<>();
    private RecyclerView recyclerView;
    private RvAdapter rAdapter;
    private RecyclerView.LayoutManager rLayoutManger;
    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    private View homeView;
    private View noFriendsTextView;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActivity().setTitle(R.string.friends_trail_feed);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        homeView = inflater.inflate(R.layout.fragment_home, container, false);

        createTrailsList();
        createRecyclerView(homeView);

        rAdapter.setOnItemClickListener(new RvAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), TrailDetailActivity.class);
                Bundle b = new Bundle();

                //TODO For some reason sending a base64 image string via a bundle causes an error.  Need to investigate reason.  Changing image.
//                trails.get(position).setTrailImageFilename("image");
                b.putSerializable("trail", recyclerTrails.get(position).getTrail());
                intent.putExtra("bundle", b);
                startActivity(intent);
//                rAdapter.notifyDataSetChanged();
            }
        });

        this.noFriendsTextView = homeView.findViewById(R.id.noFriendsTextView);

        return homeView;
    }

    public void createTrailsList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String name = currentUser.getDisplayName();
            GetFriends getFriends = new GetFriends();
            getFriends.getFriends(name);
        }
    }

    public void createRecyclerView(View homeView) {
        recyclerView = homeView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        rLayoutManger = new LinearLayoutManager(homeView.getContext());
        rAdapter = new RvAdapter(getActivity(), recyclerTrails);

        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(rLayoutManger);
    }

    @Override
    public int getTitle() {
        return R.string.friends_trail_feed;
    }

    private class GetTrails implements Runnable {

        private String username;

        GetTrails() {
        }

        public void getTrails(String username) {
            this.username = username;
            this.runInNewThread();
        }

        @Override
        public void run() {
            databaseReference.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    List<Trail> userTrails = user.getTrails();
                    if (userTrails == null) {
                        userTrails = new ArrayList<>();
                    }

                    List<Trail> sortedUserTrails = sortTrailsByTimestamp(userTrails);
                    for (Trail trail : sortedUserTrails) {
                        recyclerTrails.add(new RecyclerTrail(username, trail));
                    }

                    if (recyclerTrails.size() == 0) {
                        noFriendsTextView.setVisibility(View.VISIBLE);
                    } else {
                        noFriendsTextView.setVisibility(View.GONE);
                    }

                    rAdapter.notifyDataSetChanged();

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
                    Toast.makeText(homeView.getContext(), toastText, Toast.LENGTH_LONG).show();
                }
            });
        }

        public void runInNewThread() {
            new Thread(this).start();
        }

        private List<Trail> sortTrailsByTimestamp(List<Trail> userTrails) {
            Collections.sort(userTrails, new Comparator<Trail>() {

                @Override
                public int compare(Trail o1, Trail o2) {
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                }
            });

            return userTrails;
        }
    }

    private class GetFriends implements Runnable {

        private String username;

        GetFriends() {
        }

        public void getFriends(String username) {
            this.username = username;
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
                    } else {
                        for (String friend : friends) {
                            GetTrails getTrails = new GetTrails();
                            getTrails.getTrails(friend);
                        }
                    }
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
                    Toast.makeText(homeView.getContext(), toastText, Toast.LENGTH_LONG).show();
                }
            });
        }

        public void runInNewThread() {
            new Thread(this).start();
        }
    }

}