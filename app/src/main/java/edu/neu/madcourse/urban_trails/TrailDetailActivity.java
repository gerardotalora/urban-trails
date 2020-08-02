package edu.neu.madcourse.urban_trails;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import edu.neu.madcourse.urban_trails.models.Trail;

public class TrailDetailActivity extends AppCompatActivity {

    Trail trail;
    ImageView trailImage;
    TextView trailName;
    TextView trailDescritpion;
    TextView trailCreatedTime;
    TextView trailStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_detail);

        this.trail = (Trail) getIntent().getBundleExtra("bundle").getSerializable("trail");

        trailImage = findViewById(R.id.image_trail);
        trailName = findViewById(R.id.text_trail_description);
        trailDescritpion = findViewById(R.id.text_trail_name);
        trailCreatedTime = findViewById(R.id.text_trail_time_created);
        trailStops = findViewById(R.id.text_trail_stops);

        trailName.setText(trail.getName());
        trailDescritpion.setText(trail.getDescription());
//        trailCreatedTime.setText(Long.toString(trail.getTimestamp()));
//        trailStops.setText(Integer.toString(trail.getStops().size()));

    }
}