package edu.neu.madcourse.urban_trails;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class TrailActivity extends AppCompatActivity {

    private static final String KEY_MAPS_FRAGMENT = "KEY_MAPS_FRAGMENT";

    private MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail);

        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mapsFragment = (MapsFragment) getSupportFragmentManager().getFragment(savedInstanceState, KEY_MAPS_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mapsFragment != null) {
            //Save the fragment's instance
            getSupportFragmentManager().putFragment(outState, KEY_MAPS_FRAGMENT, mapsFragment);
        }
    }
}