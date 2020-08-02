package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.neu.madcourse.urban_trails.models.Stop;
import edu.neu.madcourse.urban_trails.models.Trail;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String STATE_KEY_MAP_CAMERA = "STATE_KEY_MAP_CAMERA";
    private static final String STATE_KEY_TRAIL = "STATE_KEY_TRAIL";
    private static final boolean STATIONARY_TESTING = false; // TODO This flag controls whether you need to walk or not

    GoogleMap map;
    private LocationManager locationManager;
    CameraPosition previousCameraPosition;
    private LatLng myLocation;

    private Trail trail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        this.locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        this.trail = new Trail();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case, we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to
     * install it inside the SupportMapFragment. This method will only be triggered once the
     * user has installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setOnInfoWindowClickListener(this);
        this.enableMyLocation();
        this.displayTrailOnMap();
    }

    /**
     * Gets called when the user clicks "Add Stop" in the interface.
     * Should add a stop to the trail at my location.
     */
    public void addStop() {
        if (myLocation != null) {
            LatLng latLng;

            // FOR TESTING
            if (STATIONARY_TESTING) {
                if (this.trail.getStops().size() > 0) {
                    latLng = shiftLatLngByFeet(this.trail.getStops().get(this.trail.getStops().size() - 1).getLatLng(), 100, 9);
                } else {
                    latLng = shiftLatLngByFeet(myLocation, 101, 0);
                }
            } else {
                if (this.trail.getStops().size() > 0 && myLocation == this.trail.getStops().get(this.trail.getStops().size() - 1).getLatLng()) {
                    Toast.makeText(getActivity(), "You already added this stop! Try walking a little farther.", Toast.LENGTH_LONG).show();
                }
                latLng = myLocation;
            }


            Stop stop = new Stop("Stop " + this.trail.getStops().size(), latLng);
            this.trail.getStops().add(stop);
        } else {
            Toast.makeText(getActivity(), "myLocation is null", Toast.LENGTH_LONG).show();
        }
        this.displayTrailOnMap();
    }

    public void getTrail(final TrailActivity parent) {
        final Trail trail = this.trail;
        this.map.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                parent.onEndTrailCallback(trail, bitmap);
            }
        });
    }

    private LatLng shiftLatLngByFeet(LatLng in, double latShiftFeet, double longShiftFeet) {
        double latShiftDegrees = latShiftFeet / (70 * 5280);
        double longShiftDegrees = longShiftFeet / (70 * 5280);
        return new LatLng(in.latitude + latShiftDegrees, in.longitude + longShiftDegrees);
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        this.map.setMyLocationEnabled(true);
        subscribeToLocation();
    }

    private void zoomToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 16.0f)));
    }

    /**
     * This method gets called whenever Android gives us an update on the user's location
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
//        Toast.makeText(getActivity(), "Location updated at " + dtf.format(LocalDateTime.now()), Toast.LENGTH_SHORT).show();
        this.myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.trail.getStops().size() == 0) {
            Stop stop = new Stop("Starting Location", this.myLocation);
            this.trail.getStops().add(stop);
            this.displayTrailOnMap();
        }
        zoomToLocation(location.getLatitude(), location.getLongitude());
    }

    private void displayTrailOnMap() {
        this.map.clear();
        for (Stop stop : this.trail.getStops()) {
            this.map.addMarker(new MarkerOptions().position(stop.getLatLng()).title(stop.getTitle()));
        }
    }


    private void subscribeToLocation() {
        Log.v("DEBUG", "subscribeToLocation");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

//        String enabledProvider = locationManager.getBestProvider(criteria, true);
        String enabledProvider = locationManager.NETWORK_PROVIDER;

        if (enabledProvider == null) {
            Log.v("DEBUG", "Best provider is null");
            return;
        }

        Log.v("DEBUG", "locationManager.requestSingleUpdate");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(enabledProvider, 3000, 3, this, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(getActivity(),
                        "Fine Location Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();

                enableMyLocation();
            } else {
                Toast.makeText(getActivity(),
                        "Fine Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * This method must restore the fragment state (e.g. after switching between portrait and landscape mode)
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.previousCameraPosition = (CameraPosition) savedInstanceState.get(STATE_KEY_MAP_CAMERA);
            this.trail = (Trail) savedInstanceState.get(STATE_KEY_TRAIL);
        }
    }

    /**
     * This method must save the fragment state before switching between portrait and landscape mode
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.map != null) {
            outState.putParcelable(STATE_KEY_MAP_CAMERA, this.map.getCameraPosition());
        }
        outState.putSerializable(STATE_KEY_TRAIL, this.trail);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
//        Toast.makeText(getActivity(), "Context", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), EditStopActivity.class);
        startActivityForResult(intent, 0);
    }
}