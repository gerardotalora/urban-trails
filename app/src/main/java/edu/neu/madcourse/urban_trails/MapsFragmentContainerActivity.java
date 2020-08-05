package edu.neu.madcourse.urban_trails;

import com.google.android.gms.maps.GoogleMap;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

public interface MapsFragmentContainerActivity extends GoogleMap.InfoWindowAdapter {
    void stopClicked(Stop stop);

    void onEndTrailCallback(Trail trail);
}
