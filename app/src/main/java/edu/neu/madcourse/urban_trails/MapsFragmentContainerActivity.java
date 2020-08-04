package edu.neu.madcourse.urban_trails;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

public interface MapsFragmentContainerActivity {
    void stopClicked(Stop stop);

    void onEndTrailCallback(Trail trail);
}
