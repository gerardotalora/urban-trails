package edu.neu.madcourse.urban_trails.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import edu.neu.madcourse.urban_trails.R;
import edu.neu.madcourse.urban_trails.RvAdapter;
import edu.neu.madcourse.urban_trails.models.Trail;
import edu.neu.madcourse.urban_trails.models.User;

public class HomeFragment extends Fragment {

    private final String TAG = "Home Fragment";

    private ArrayList<Trail> trails = new ArrayList<>();
    private RecyclerView recyclerView;
    private RvAdapter rAdapter;
    private RecyclerView.LayoutManager rLayoutManger;
    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    private View homeView;

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
                Toast.makeText(homeView.getContext(), trails.get(position).getName(), Toast.LENGTH_LONG).show();
//                rAdapter.notifyDataSetChanged();
            }
        });

        return homeView;
    }

    public void createTrailsList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String name = currentUser.getDisplayName();
            GetTrails getTrails = new GetTrails();
            getTrails.getTrails(name);
        }
    }

    public void createRecyclerView(View homeView) {
        recyclerView = homeView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        rLayoutManger = new LinearLayoutManager(homeView.getContext());
        rAdapter = new RvAdapter(trails);

        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(rLayoutManger);
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
                    trails.addAll(userTrails);
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
    }
}