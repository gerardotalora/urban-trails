package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

public class FollowTrailActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, MapsFragmentContainerActivity {

    BottomNavigationView bottomNavigation;
    private MapsFragment mapsFragment;
    private Trail trail;
    private Map<Marker, View> lastInfoWindowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_trail);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        this.trail = (Trail) getIntent().getBundleExtra("bundle").getSerializable("trail");
        
        
        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        mapsFragment.setTrail(this.trail);

        this.lastInfoWindowView = new HashMap<>();
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
    public View getInfoContents(final Marker marker) {
        if (this.lastInfoWindowView != null) {
            View res = this.lastInfoWindowView.get(marker);
            if (res != null) {
                this.lastInfoWindowView.put(marker, null);
                return res;
            }
        }
//        return null;
        View view = getLayoutInflater().inflate(R.layout.custom_info_window_follow_trail, null);
        TextView textView = view.findViewById(R.id.stopTitle);
        final ImageView imageView = view.findViewById(R.id.stopImage);
        if (marker.getTag() instanceof Stop) {
            Stop stop = (Stop) marker.getTag();
            textView.setText(stop.getTitle());
            if (stop.getImageFileName() == null) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                final Context context = this;
//                String filename = Uri.fromFile(new File(stop.getImageFileName())).getLastPathSegment();
                String filename = stop.getImageFileName();
                this.lastInfoWindowView.put(marker, view);
                Utils.displayThumbnail(getApplicationContext(), imageView, filename, new OnImageDrawnListener() {
                    @Override
                    public void onImageDrawn() {
                        marker.showInfoWindow();
                    }
                }, 300, 300);
            }
        }
        return view;
    }
}