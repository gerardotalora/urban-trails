package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

public class TrailActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, MapsFragmentContainerActivity {

    private MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail);

        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        switch (menuItem.getItemId()) {
            case R.id.navigation_add_stop:
//                Toast.makeText(this, "Navigation Add Stop", Toast.LENGTH_LONG).show();
                mapsFragment.addStop();
                break;
//            case R.id.navigation_camera:
//                Toast.makeText(this, "Navigation Camera", Toast.LENGTH_LONG).show();
//                break;
            case R.id.navigation_end_trail:
//                Toast.makeText(this, "Navigation End Trail", Toast.LENGTH_LONG).show();
                mapsFragment.getTrail(this); // Calls onEndTrailCallback
                break;
        }
        return false;
    }

    public void onEndTrailCallback(Trail trail) {
        try {
            Intent intent = new Intent(this, TrailSummaryActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("trail", trail);
            intent.putExtra("bundle", b);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopClicked(Stop stop) {
        Intent intent = new Intent(this, EditStopActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("stop", stop);
        intent.putExtra("bundle", b);
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Stop stop = (Stop) data.getBundleExtra("bundle").getSerializable("stop");
                this.mapsFragment.updateStopInfo(stop);
            }
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}