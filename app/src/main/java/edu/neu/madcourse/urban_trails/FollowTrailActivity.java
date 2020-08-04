package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

public class FollowTrailActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, MapsFragmentContainerActivity {

    BottomNavigationView bottomNavigation;
    private MapsFragment mapsFragment;
    private Trail trail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_trail);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        this.trail = (Trail) getIntent().getBundleExtra("bundle").getSerializable("trail");
        
        
        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        mapsFragment.setTrail(this.trail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        if (menuItem.getItemId() == R.id.follow_trail_cancel) {
            finish();
        }
        return false;
    }

    @Override
    public void stopClicked(Stop stop) {
        Intent intent = new Intent(this, ViewStopActivity.class);
        startActivity(intent);
    }

    @Override
    public void onEndTrailCallback(Trail trail) {
        // Not applicable
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
//        return null;
        View view = getLayoutInflater().inflate(R.layout.custom_info_window_follow_trail, null);
        TextView textView = view.findViewById(R.id.stopTitle);
        ImageView imageView = view.findViewById(R.id.stopImage);
        if (marker.getTag() instanceof Stop) {
            Stop stop = (Stop) marker.getTag();

            textView.setText(stop.getTitle());

            if (stop.getImageFileName() == null) {
                imageView.setVisibility(View.GONE);
            } else {

            }
        }
        return view;
    }
}