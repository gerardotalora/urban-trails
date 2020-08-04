package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

public class FollowTrailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_trail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        if (menuItem.getItemId() == R.id.follow_trail_cancel) {
            finish();
        }
        return false;
    }
}