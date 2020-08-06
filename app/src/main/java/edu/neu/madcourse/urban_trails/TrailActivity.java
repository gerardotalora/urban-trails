package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

import java.util.HashMap;
import java.util.Map;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

public class TrailActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, MapsFragmentContainerActivity {

    private MapsFragment mapsFragment;
    private Map<Marker, View> lastInfoWindowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail);

        mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        this.lastInfoWindowView = new HashMap<>();
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
    public View getInfoContents(final Marker marker) {
        if (this.lastInfoWindowView != null) {
            View res = this.lastInfoWindowView.get(marker);
            if (res != null) {
                this.lastInfoWindowView.put(marker, null);
                return res;
            }
        }
        View view = getLayoutInflater().inflate(R.layout.custom_info_window_create_trail, null);
        TextView textView = view.findViewById(R.id.stopTitle2);
        final ImageView imageView = view.findViewById(R.id.stopImage2);
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

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TrailActivity.this);
        builder.setMessage("Are you sure you want to exit your trail?");
        builder.setCancelable(true);
        builder.setNegativeButton("Resume Trail", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Exit Trail", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}