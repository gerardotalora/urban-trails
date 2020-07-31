package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import com.google.firebase.auth.FirebaseAuth;

import edu.neu.madcourse.urban_trails.fragments.HomeFragment;
import edu.neu.madcourse.urban_trails.fragments.ProfileFragment;
import edu.neu.madcourse.urban_trails.fragments.SearchFragment;
import edu.neu.madcourse.urban_trails.fragments.StarFragment;
import edu.neu.madcourse.urban_trails.fragments.StartTrailFragment;

public class TrailActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    private static final String KEY_MAPS_FRAGMENT = "KEY_MAPS_FRAGMENT";

    private MapsFragment mapsFragment;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail);

        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mapsFragment = (MapsFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_MAPS_FRAGMENT);
        }

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mapsFragment != null) {
            //Save the fragment's instance
            getSupportFragmentManager().putFragment(outState, KEY_MAPS_FRAGMENT, mapsFragment);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_add_stop:
                Toast.makeText(this, "Navigation Add Stop", Toast.LENGTH_LONG).show();
                break;
            case R.id.navigation_camera:
                Toast.makeText(this, "Navigation Camera", Toast.LENGTH_LONG).show();
                break;
            case R.id.navigation_end_trail:
                Toast.makeText(this, "Navigation End Trail", Toast.LENGTH_LONG).show();
                break;
        }
        return false;
    }
}