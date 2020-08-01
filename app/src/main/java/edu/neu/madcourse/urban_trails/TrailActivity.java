package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import java.io.FileOutputStream;

import edu.neu.madcourse.urban_trails.models.Trail;

public class TrailActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

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
            case R.id.navigation_camera:
                Toast.makeText(this, "Navigation Camera", Toast.LENGTH_LONG).show();
                break;
            case R.id.navigation_end_trail:
//                Toast.makeText(this, "Navigation End Trail", Toast.LENGTH_LONG).show();
                mapsFragment.getTrail(this); // Calls onEndTrailCallback
                break;
        }
        return false;
    }

    public void onEndTrailCallback(Trail trail, Bitmap snapshot) {
        try {
            //Write file
            String filename = "bitmap.png";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            snapshot.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            snapshot.recycle();

            //Pop intent
            Intent intent = new Intent(this, TrailSummaryActivity.class);

            Bundle b = new Bundle();
            b.putSerializable("trail", trail);
            intent.putExtra("bundle", b);
            intent.putExtra("image", filename);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}